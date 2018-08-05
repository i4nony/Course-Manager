package com.wumii.wechat.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.wumii.wechat.dao.TeacherDao;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.Teacher;
import com.wumii.wechat.exception.WeChatOperationException;
import com.wumii.wechat.util.CommonUtil;
import com.wumii.wechat.util.Constant;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Service
public class WeChatSnsTimeLineService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatSnsTimeLineService.class);

    private static final String SNS_POST_XML =
            "<TimelineObject>\n" +
                    "    <id>0</id>\n" +
                    "    <username>%s</username>\n" +
                    "    <createTime>%d</createTime>\n" +
                    "    <contentDesc>%s</contentDesc>\n" +
                    "    <contentDescShowType>0</contentDescShowType>\n" +
                    "    <contentDescScene>3</contentDescScene>\n" +
                    "    <private>0</private>\n" +
                    "    <sightFolded>0</sightFolded>\n" +
                    "    <appInfo>\n" +
                    "        <id></id>\n" +
                    "        <version></version>\n" +
                    "        <appName></appName>\n" +
                    "        <installUrl></installUrl>\n" +
                    "        <fromUrl></fromUrl>\n" +
                    "        <isForceUpdate>0</isForceUpdate>\n" +
                    "    </appInfo>\n" +
                    "    <sourceUserName></sourceUserName>\n" +
                    "    <sourceNickName></sourceNickName>\n" +
                    "    <statisticsData></statisticsData>\n" +
                    "    <statExtStr></statExtStr>\n" +
                    "    <ContentObject>\n" +
                    "        <contentStyle>1</contentStyle>\n" +
                    "        <title></title>\n" +
                    "        <description></description>\n" +
                    "        <mediaList>\n" +
                    "            %s\n" +
                    "        </mediaList>\n" +
                    "    </ContentObject>\n" +
                    "</TimelineObject>";

    private static final String SNS_MEDIA_XML =
            "<media>\n" +
                    "                <id>0</id>\n" +
                    "                <type>2</type>\n" +
                    "                <title></title>\n" +
                    "                <description></description>\n" +
                    "                <private>0</private>\n" +
                    "                <userData></userData>\n" +
                    "                <subType>0</subType>\n" +
                    "                <url type=\"1\" md5=\"%s\" videomd5=\"\">\n" +
                    "                    %s" +
                    "                </url>\n" +
                    "                <thumb type=\"1\">\n" +
                    "                    %s" +
                    "                </thumb>\n" +
                    "                <size width=\"%d\" height=\"%d\" totalSize=\"0\"/>\n" +
                    "            </media>";
    private static final Random random = new Random();

    private final TeacherDao teacherDao;
    private final WeChatLoginService weChatLoginService;

    public WeChatSnsTimeLineService(TeacherDao teacherDao,
                                    WeChatLoginService weChatLoginService) {
        this.teacherDao = teacherDao;
        this.weChatLoginService = weChatLoginService;
    }

    public static class SnsImage {
        private String url;
        private String thumbUrl;
        private int width;
        private int height;
        private String md5;

        public SnsImage(String url, String thumbUrl, int width, int height, String md5) {
            this.url = url;
            this.thumbUrl = thumbUrl;
            this.width = width;
            this.height = height;
            this.md5 = md5;
        }
        // TODO: 7/12/18 这些数据需要图片服务提供
    }

    void sendSnsTimeLine(WeChatClient client, String content, List<SnsImage> images) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.SnsPostRequest request = buildSnsPostRequest(client, content, images);
        Dto.SnsPostResponse response = Dto.SnsPostResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/mmsnspost", request.toByteArray(),
                        209, false));
        if (response.getResult().getCode() != 0) {
            throw new WeChatOperationException("send sns time line fail");
        }
    }

    private Dto.SnsPostRequest buildSnsPostRequest(WeChatClient client, String content, List<SnsImage> images) {
        StringBuilder sb = new StringBuilder();
        for (SnsImage image : images) {
            sb.append(String.format(SNS_MEDIA_XML, image.md5, image.url, image.thumbUrl, image.width, image.height));
        }
        String requestXml = String.format(SNS_POST_XML, client.getWxid(), CommonUtil.now(), content, sb.toString());
        Dto.SnsPostRequest request = Dto.SnsPostRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setTimeLineObject(Dto.SnsPostRequest.TimeLineObject.newBuilder()
                        .setLen(requestXml.getBytes().length)
                        .setXml(requestXml)
                        .build())
                .setWithUserListCount(0)
                .setPrivacy(0)
                .setSyncFlag(0)
                .setClientId("sns_post_" + client.getWxid() + "_" + CommonUtil.now()
                        + "_" + random.nextInt(Integer.MAX_VALUE))
                .setPostBgImgType(1)
                .setObjectSource(0)
                .setBlackLiskCount(0)
                .setGroupUserCount(0)
                .setFields(Dto.SnsPostRequest.SnsPostOperationFields.newBuilder()
                        .setUnknown1(ByteString.EMPTY)
                        .setUnknown2(ByteString.EMPTY)
                        .setUnknown3(ByteString.EMPTY)
                        .setUnknown4(0)
                        .setUnknown5(0)
                        .build())
                .setPoiInfo(Dto.SnsPostRequest.PoiInfo.newBuilder()
                        .setUnknown1(0)
                        .setUnknown2(ByteString.EMPTY)
                        .build())
                .setMediaCount(images.size())
                .setWeAppInfo(ByteString.EMPTY)
                .build();
        for (int i = 0; i < images.size(); i++) {
            request = request.toBuilder()
                    .addMediaInfo(Dto.SnsPostRequest.MediaInfo.newBuilder()
                            .setUnknown1(2)
                            .setUnknown2(1)
                            .build())
                    .build();
        }
        return request;
    }

    public boolean sendSnsTimeLine(String content, List<SnsImage> images) throws InterruptedException {
        WeChatClient client = weChatLoginService.getLoginClient();
        return client.submit(() -> sendSnsTimeLine(client, content, images)).isSuccess();
    }

    void updateSnsTimeLine(WeChatClient client) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.UpdateSnsTimeLineRequest request = buildUpdateSnsTimeLineRequest(client);
        Dto.UpdateSnsTimeLineResponse response = Dto.UpdateSnsTimeLineResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/mmsnstimeline", request.toByteArray(),
                        211, false));
        if (response.getResult().getCode() != 0) {
            throw new WeChatOperationException("update sns time line error");
        }
        Teacher teacher = client.getTeacher();
        if (response.getCount() > 0) {
            List<Dto.UpdateSnsTimeLineResponse.SnsTimeLine> snsTimeLineList = response.getSnsTimeLineList();
            String firstPageMD5 = response.getFirstPageMD5();
            long clientId = snsTimeLineList.get(0).getClientId();
            teacher.setTimeLineMD5(firstPageMD5);
            teacher.setTimeLineId(clientId);
            teacherDao.save(teacher);
            saveSnsTimeLine(client, snsTimeLineList);
        }
    }

    private Dto.UpdateSnsTimeLineRequest buildUpdateSnsTimeLineRequest(WeChatClient client) {
        Teacher teacher = client.getTeacher();
        return Dto.UpdateSnsTimeLineRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setFirstPageMD5(Strings.nullToEmpty(teacher.getTimeLineMD5()))
                .setUnknown3(0)
                .setUnknown4(0)
                .setUnknown5(0)
                .setClientLastestId(teacher.getTimeLineId())
                .setUnknown7(Dto.UpdateSnsTimeLineRequest.Unknown7.newBuilder()
                        .setUnknown1(0)
                        .setUnknown2(ByteString.EMPTY)
                        .build())
                .setUnknown8(1)
                .setUnknown10(ByteString.EMPTY)
                .build();
    }

    private void saveSnsTimeLine(WeChatClient client, List<Dto.UpdateSnsTimeLineResponse.SnsTimeLine> snsTimeLineList) {
        Preconditions.checkState(client.inEventLoop());
        try {
            for (Dto.UpdateSnsTimeLineResponse.SnsTimeLine snsTimeLine : snsTimeLineList) {
                String wxid = snsTimeLine.getWxid();
                Document document = DocumentHelper.parseText(snsTimeLine.getContent().getXml());
                Element rootElement = document.getRootElement();
                // TODO: 7/12/18 根据打卡朋友圈类型存储
            }
        } catch (DocumentException e) {
            logger.error("parse xml error", e);
        }
    }

}
