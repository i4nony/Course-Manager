package com.wumii.wechat.service;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.wumii.wechat.dao.TeacherDao;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.Teacher;
import com.wumii.wechat.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class WeChatSyncService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatSyncService.class);

    private final TeacherDao teacherDao;
    private final WeChatFriendService weChatFriendService;
    private final WeChatMessageService weChatMessageService;

    @Autowired
    public WeChatSyncService(TeacherDao teacherDao,
                             WeChatFriendService weChatFriendService,
                             WeChatMessageService weChatMessageService) {
        this.teacherDao = teacherDao;
        this.weChatFriendService = weChatFriendService;
        this.weChatMessageService = weChatMessageService;
    }

    void weChatInit(WeChatClient client) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Teacher teacher = client.getTeacher();
        boolean continueFlag;
        do {
            Dto.InitRequest request = buildInitRequest(client);
            Dto.InitResponse initResponse = Dto.InitResponse.parseFrom(
                    client.sendRequest("/cgi-bin/micromsg-bin/newinit", request.toByteArray(),
                            139, false));
            parseResponse(client, initResponse.getCommonResponseList());
            teacher.setSyncKey(initResponse.getSyncKeyCur().toByteArray());
            teacherDao.save(teacher);
            continueFlag = initResponse.getContinueFlag() == 1;
        } while (continueFlag);
    }

    private Dto.InitRequest buildInitRequest(WeChatClient client) {
        Teacher teacher = client.getTeacher();
        ByteString syncKey = teacher.getSyncKey() == null ?
                ByteString.EMPTY : ByteString.copyFrom(teacher.getSyncKey());
        return Dto.InitRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(3)
                        .build())
                .setSyncKeyCur(syncKey)
                .setSyncKeyMax(syncKey)
                .setWxid(client.getWxid())
                .setLanguage(Constant.LANGUAGE)
                .build();
    }

    byte[] sync(WeChatClient client) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.SyncRequest request = buildSyncRequest(client);
        Dto.SyncResponse syncResponse = Dto.SyncResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/newsync", request.toByteArray(),
                        138, false));
        parseResponse(client, syncResponse.getMessageWrap().getCommonResponseList());
        Teacher teacher = client.getTeacher();
        teacher.setSyncKey(syncResponse.getSyncKey().toByteArray());
        teacherDao.save(teacher);

        Dto.SyncKey syncKey = Dto.SyncKey.parseFrom(syncResponse.getSyncKey());
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeByte(0x00);
        dataOutput.writeByte(0x00);
        dataOutput.writeByte(0x00);
        dataOutput.writeByte(0xff);
        dataOutput.writeInt(syncKey.getLength());
        dataOutput.write(syncKey.getMsgkey().toByteArray());
        return dataOutput.toByteArray();
    }

    private Dto.SyncRequest buildSyncRequest(WeChatClient client) {
        Teacher teacher = client.getTeacher();
        return Dto.SyncRequest.newBuilder()
                .setOsVersion(Constant.OS_VERSION)
                .setUnknown1(Dto.SyncRequest.Unknown1.newBuilder().setUnknown1(0).build())
                .setUnknown2(7)
                .setSyncKey(ByteString.copyFrom(teacher.getSyncKey()))
                .setScene(3)
                .setUnknown6(1)
                .build();
    }

    private void parseResponse(WeChatClient client,
                               List<Dto.CommonResponse> commonResponses) throws IOException {
        Preconditions.checkState(client.inEventLoop());
        for (Dto.CommonResponse commonResponse : commonResponses) {
            switch (commonResponse.getType()) {
                case 5: {
                    Dto.CommonMessage commonMessage = Dto.CommonMessage
                            .parseFrom(commonResponse.getData().getData().toByteArray());
                    switch (commonMessage.getType()) {
                        case 1:
                            weChatMessageService.receiveTextMessage(client, commonMessage);
                            break;
                        case 3:
                            weChatMessageService.receiveImageMessage(client, commonMessage);
                            break;
                        case 34:
                            weChatMessageService.receiveAudioMessage(client, commonMessage);
                            break;
                        case 37: //好友申请
                            weChatFriendService.acceptNewFriend(client, commonMessage);
                            break;
                    }
                    break;
                }
                case 2:
                    Dto.ContactInfo contactInfo = Dto.ContactInfo
                            .parseFrom(commonResponse.getData().getData().toByteArray());
                    weChatFriendService.saveContactInfo(client, contactInfo);
                    break;
                default:
                    logger.info("ignore response: {}", commonResponse.toString());
            }
        }
    }
}
