package com.wumii.wechat.service;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.wumii.wechat.dao.MessageDao;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.ImageMessage;
import com.wumii.wechat.entity.Message;
import com.wumii.wechat.entity.StringMessage;
import com.wumii.wechat.exception.WeChatOperationException;
import com.wumii.wechat.util.CommonUtil;
import com.wumii.wechat.util.Constant;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@Service
public class WeChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatMessageService.class);
    private static final Random random = new Random();

    private final MessageDao messageDao;
    private final WeChatLoginService weChatLoginService;

    @Autowired
    public WeChatMessageService(MessageDao messageDao,
                                WeChatLoginService weChatLoginService) {
        this.messageDao = messageDao;
        this.weChatLoginService = weChatLoginService;
    }

    void sendTextMessage(WeChatClient client, List<String> toWxids, String content, List<String> atWxids)
            throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.SendMessageRequest request = buildSendMessageRequest(client, toWxids, content, atWxids);
        Dto.SendMessageResponse response = Dto.SendMessageResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/newsendmsg", request.toByteArray(),
                        522, false));
        for (Dto.SendMessageResponse.Result result : response.getResultList()) {
            if (result.getCode() != 0) {
                throw new WeChatOperationException("send message error" + result);
            }
            Message message = new StringMessage(client.getWxid(), result.getTo().getId(), content);
            messageDao.save(message);
        }
    }

    private Dto.SendMessageRequest buildSendMessageRequest(
            WeChatClient client, List<String> toWxids, String content, List<String> atWxids) {
        Dto.SendMessageRequest request = Dto.SendMessageRequest.newBuilder()
                .setCount(1)
                .build();
        for (String toWxid : toWxids) {
            Dto.SendMessageRequest.SendMessageInfo sendMessageInfo = Dto.SendMessageRequest.SendMessageInfo.newBuilder()
                    .setTo(Dto.Wxid.newBuilder().setId(toWxid).build())
                    .setContent(ByteString.copyFrom(content.getBytes()))
                    .setType(1)
                    .setTimestamp(CommonUtil.now())
                    .setClientId(random.nextInt(Integer.MAX_VALUE))
                    .setAt((atWxids == null || atWxids.isEmpty()) ? "" : "<msgsource><atuserlist><![CDATA["
                            + Joiner.on(",").join(atWxids) + "]]></atuserlist></msgsource>")
                    .build();
            request = request.toBuilder().addInfo(sendMessageInfo).build();
        }
        return request;
    }

    public boolean sendTextMessage(List<String> toWxids, String content, List<String> atWxids) throws InterruptedException {
        WeChatClient client = weChatLoginService.getLoginClient();
        return client.submit(() -> sendTextMessage(client, toWxids, content, atWxids)).isSuccess();
    }

    public boolean sendImageMessage(List<String> toWxids, String picUrl) throws IOException, InterruptedException {
        WeChatClient client = weChatLoginService.getLoginClient();
        byte[] picBytes = CommonUtil.httpGet(picUrl);
        boolean result = true;
        for (String wxid : toWxids) {
            result = result && client.submit(()
                    -> sendImageMessage(client, wxid, picUrl, picBytes)).isSuccess();
        }
        return result;
    }

    void sendImageMessage(WeChatClient client, String wxid, String picUrl, byte[] picBytes) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        String clientImgId = client.getWxid() + random.nextInt(10) + "_" + CommonUtil.now();
        for (int startPos = 0; startPos < picBytes.length; startPos = startPos + 8192) {
            int length = Math.min(picBytes.length - startPos, 8192);
            Dto.UploadMsgImgRequest request = buildUploadMsgImgRequest(client, wxid, picBytes, startPos, length, clientImgId);
            Dto.UploadMsgImgResponse response = Dto.UploadMsgImgResponse.parseFrom(
                    client.sendRequest("/cgi-bin/micromsg-bin/uploadmsgimg", request.toByteArray(),
                            110, true));
            if (response.getResult().getCode() != 0) {
                throw new WeChatOperationException("send image message fail");
            }
        }
        Message message = new ImageMessage(client.getWxid(), wxid, picUrl);
        messageDao.save(message);
    }

    private Dto.UploadMsgImgRequest buildUploadMsgImgRequest(WeChatClient client, String wxid,
                                                             byte[] picBytes,
                                                             int startPos, int length,
                                                             String clientImgId) {
        return Dto.UploadMsgImgRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setFrom(Dto.Wxid.newBuilder()
                        .setId(client.getWxid())
                        .build())
                .setTo(Dto.Wxid.newBuilder()
                        .setId(wxid)
                        .build())
                .setClientImgId(Dto.UploadMsgImgRequest.ClientImgId.newBuilder()
                        .setClientImgId(clientImgId)
                        .build())
                .setLength(picBytes.length)
                .setDataLength(length)
                .setData(Dto.Data.newBuilder()
                        .setLen(length)
                        .setData(ByteString.copyFrom(Arrays.copyOfRange(picBytes, startPos, startPos + length)))
                        .build())
                .setStartPos(startPos)
                .setType(3)
                .setCompressType(0)
                .setNetType(1)
                .setPhotoFrom(0)
                .setMsgForwardType(0)
                .build();
    }

    void receiveTextMessage(WeChatClient client, Dto.CommonMessage commonMessage) {
        Preconditions.checkState(client.inEventLoop());

        String fromWxid = commonMessage.getFrom().getId();
        String toWxid = commonMessage.getTo().getId();
        String content = commonMessage.getRaw().getContent();
        if (Constant.SYSTEM_WXID.contains(fromWxid)) {
            return; //系统消息不存数据库
        }
        Message message = new StringMessage(fromWxid, toWxid, content);
        messageDao.save(message);
    }

    void receiveAudioMessage(WeChatClient client, Dto.CommonMessage commonMessage) {
        Preconditions.checkState(client.inEventLoop());
        byte[] audio = commonMessage.getAddtionalContent().toByteArray();
    }

    void receiveImageMessage(WeChatClient client, Dto.CommonMessage commonMessage) throws IOException {
        Preconditions.checkState(client.inEventLoop());
        String fromWxid = commonMessage.getFrom().getId();
        String toWxid = commonMessage.getTo().getId();
        int msgId = commonMessage.getMsgId();
        long newMsgId = commonMessage.getNewMsgId();
        String xml = commonMessage.getRaw().getContent();
        try {
            Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            Element img = rootElement.element("img");
            int length = Integer.parseInt(img.attributeValue("length"));
            byte[] image = getImage(client, length, fromWxid, toWxid, newMsgId, msgId);

            // TODO: 7/11/18 图片服务
            if (image != null) {
                Files.write(Paths.get("/home/liliyuan/Desktop/test.jpg"), image);
            }
            Message message = new ImageMessage(fromWxid, toWxid, "TODO");
            messageDao.save(message);
        } catch (DocumentException e) {
            logger.error("parse xml error", e);
        }
    }

    private byte[] getImage(WeChatClient client, int totalLen, String from, String to, long newMsgId, int msgId)
            throws IOException {
        Preconditions.checkState(client.inEventLoop());

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        for (int startPos = 0; startPos < totalLen; startPos = startPos + 65536) {
            int length = Math.min(totalLen - startPos, 65536);
            Dto.GetMsgImgRequest request = buildGetMsgImgRequest(client, totalLen, from, to,
                    newMsgId, msgId, startPos, length);
            Dto.GetMsgImgResponse response = Dto.GetMsgImgResponse.parseFrom(
                    client.sendRequest("/cgi-bin/micromsg-bin/getmsgimg", request.toByteArray(),
                            109, false));
            if (response.getResult().getCode() != 0) {
                throw new WeChatOperationException("get image fail");
            }
            output.write(response.getData().getData().toByteArray());
        }
        return output.toByteArray();
    }

    private Dto.GetMsgImgRequest buildGetMsgImgRequest(WeChatClient client, int totalLen,
                                                       String from, String to,
                                                       long newMsgId, int msgId,
                                                       int startPos, int length) {
        return Dto.GetMsgImgRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setFrom(Dto.Wxid.newBuilder()
                        .setId(from)
                        .build())
                .setTo(Dto.Wxid.newBuilder()
                        .setId(to)
                        .build())
                .setLength(totalLen)
                .setStartPos(startPos)
                .setDataLength(length)
                .setCompressType(0)
                .setMsgId(msgId)
                .setNewMsgId(newMsgId)
                .build();
    }
}
