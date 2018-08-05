package com.wumii.wechat.service;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.wumii.application.exception.ShowOnlyErrorMobileResultException;
import com.wumii.application.util.DateTimeUtils;
import com.wumii.wechat.dao.IPadDao;
import com.wumii.wechat.dao.TeacherDao;
import com.wumii.wechat.dao.WechatUserDao;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.IPad;
import com.wumii.wechat.entity.Teacher;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.util.CommonUtil;
import com.wumii.wechat.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WeChatLoginService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatLoginService.class);

    private Set<LoginCheck> waitingForCheck = Sets.newConcurrentHashSet();
    private Set<WeChatClient> loginClients = Sets.newConcurrentHashSet();

    private ThreadLocal<WeChatClient> currentClient = new ThreadLocal<>();

    private final DNSService dnsService;
    private final IPadDao iPadDao;
    private final WechatUserDao wechatUserDao;
    private final TeacherDao teacherDao;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationContext context;

    @Autowired
    public WeChatLoginService(DNSService dnsService,
                              IPadDao iPadDao,
                              WechatUserDao wechatUserDao,
                              TeacherDao teacherDao,
                              TransactionTemplate transactionTemplate,
                              ApplicationContext context) {
        this.dnsService = dnsService;
        this.iPadDao = iPadDao;
        this.wechatUserDao = wechatUserDao;
        this.teacherDao = teacherDao;
        this.transactionTemplate = transactionTemplate;
        this.context = context;
    }

    @PostConstruct
    public void init() {
        List<Teacher> teachers = teacherDao.findBySessionKeyIsNotNullAndDeletedIsFalse();
        for (Teacher teacher : teachers) {
            IPad iPad = iPadDao.findFirstByOccupiedIsFalse();
            if (iPad != null) {
                WeChatClient client = new WeChatClient(context, iPad, teacher);
                client.start();
                continue;
            }
            logger.error("no enough ipad");
        }
    }

    @PreDestroy
    public void shutdown() {
        loginClients.forEach(WeChatClient::shutdown);
    }

    private class LoginCheck {
        String qrCodeKey;
        IPad iPad;
        byte[] originalAesKey;
        byte[] userInfoAesKey;

        private LoginCheck(String qrCodeKey, IPad iPad,
                           byte[] originalAesKey, byte[] userInfoAesKey) {
            this.qrCodeKey = qrCodeKey;
            this.iPad = iPad;
            this.originalAesKey = originalAesKey;
            this.userInfoAesKey = userInfoAesKey;
        }
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void cleanUp() {
        loginClients.removeIf(client -> {
            if (client.isRunning()) {
                return false;
            }
            client.shutdown(); //幂等操作
            return true;
        });
        waitingForCheck.removeIf(client -> {
            IPad iPad = client.iPad;
            long loginTime = iPad.getLoginTime().toEpochMilli();
            if (loginTime + 600 * 1000 > System.currentTimeMillis()) {
                return false;  //保守的过期时间（大于二维码过期时间），防止并发问题
            }
            iPad.setOccupied(false);
            iPadDao.save(iPad);
            return true;
        });
    }

    public void setCurrentWxid(String wxid) {
        for (WeChatClient client : loginClients) {
            if (Objects.equal(wxid, client.getWxid())) {
                if (client.isRunning()) {
                    currentClient.set(client);
                    return;
                }
            }
        }
        throw new ShowOnlyErrorMobileResultException("登录状态失效");
    }

    public List<String> loginTeacherWxid() {
        return loginClients.stream()
                .filter(WeChatClient::isRunning)
                .map(WeChatClient::getWxid)
                .collect(Collectors.toList());
    }

    WeChatClient getLoginClient() {
        WeChatClient client = currentClient.get();
        if (client == null || !client.isRunning()) {
            throw new ShowOnlyErrorMobileResultException("登录状态失效");
        }
        return client;
    }

    public void logout() {
        WeChatClient weChatClient = getLoginClient();
        if (weChatClient != null) {
            weChatClient.shutdown();
        }
    }

    public synchronized byte[] login() throws IOException {
        IPad iPad = iPadDao.findFirstByOccupiedIsFalse();
        if (iPad == null) {
            throw new ShowOnlyErrorMobileResultException("无可用设备");
        }
        iPad.setOccupied(true);
        iPad.setLoginTime(DateTimeUtils.now());
        iPadDao.save(iPad);
        byte[] aesKey = CommonUtil.randomAesKey();
        Dto.GetLoginQRCodeResponse qrCodeResponse = getQRCode(iPad, aesKey);
        waitingForCheck.add(new LoginCheck(qrCodeResponse.getQrCodeKey(), iPad,
                aesKey, qrCodeResponse.getAesKey().getKey().toByteArray()));
        return qrCodeResponse.getQrCode().getImage().toByteArray();
    }

    private Dto.GetLoginQRCodeResponse getQRCode(IPad iPad, byte[] aesKey) throws IOException {
        Dto.GetLoginQRCodeRequest request = buildGetLoginQRCodeRequest(iPad, aesKey);
        CommonUtil.UnpackResponse unpack = CommonUtil.unpack(
                CommonUtil.sendRequest(
                        "http://" + dnsService.getShortConnectionIP() + "/cgi-bin/micromsg-bin/getloginqrcode",
                        QRCodePack(request.toByteArray())), aesKey);
        return Dto.GetLoginQRCodeResponse.parseFrom(unpack.getBody());
    }

    private Dto.GetLoginQRCodeRequest buildGetLoginQRCodeRequest(IPad iPad, byte[] aesKey) {
        return Dto.GetLoginQRCodeRequest.newBuilder()
                    .setAesKey(Dto.AesKey.newBuilder().setSize(16).setKey(ByteString.copyFrom(aesKey)).build())
                    .setBaseRequest(Dto.BaseRequest.newBuilder()
                            .setAesKey(ByteString.EMPTY)
                            .setClientVersion(Constant.CLIENT_VERSION)
                            .setDeviceId(ByteString.copyFrom(iPad.getDeviceId()))
                            .setUin(0)
                            .setOsVersion(Constant.OS_VERSION)
                            .setScene(0)
                            .build())
                    .setUnknown3(0)
                    .build();
    }

    @Scheduled(fixedRate = 5000)
    public void loginCheck() throws IOException {
        Iterator<LoginCheck> iterator = waitingForCheck.iterator();
        while (iterator.hasNext()) {
            LoginCheck loginCheck = iterator.next();
            Dto.CheckLoginQRCodeRequest request = buildCheckLoginQRCodeRequest(loginCheck);
            CommonUtil.UnpackResponse unpack = CommonUtil.unpack(
                    CommonUtil.sendRequest(
                            "http://" + dnsService.getShortConnectionIP()
                                    + "/cgi-bin/micromsg-bin/checkloginqrcode",
                            QRCodePack(request.toByteArray())), loginCheck.originalAesKey);
            Dto.CheckLoginQRCodeResponse response = Dto.CheckLoginQRCodeResponse.parseFrom(unpack.getBody());

            if (response.getCheckResult().getContent().getSize() > 256) {
                iterator.remove();
                ByteString encryptedUserInfo = response.getCheckResult().getContent().getEncryptedUserInfo();
                Dto.UserInfo userInfo = Dto.UserInfo.parseFrom(CommonUtil.aesDecrypt(encryptedUserInfo.toByteArray(),
                        loginCheck.userInfoAesKey));
                logger.info("check {} success, start wechat client", userInfo.getWxid());

                Teacher teacher = createOrUpdateEntity(userInfo);

                WeChatClient weChatClient = new WeChatClient(context,
                        loginCheck.iPad,
                        teacher,
                        userInfo.getEncryptedPassword(),
                        loginCheck.originalAesKey);
                ensureUnique(weChatClient);
                weChatClient.start();
            }
        }
    }

    private Dto.CheckLoginQRCodeRequest buildCheckLoginQRCodeRequest(LoginCheck loginCheck) {
        return Dto.CheckLoginQRCodeRequest.newBuilder()
                        .setAesKey(Dto.AesKey.newBuilder().setSize(16).setKey(
                                ByteString.copyFrom(loginCheck.originalAesKey)).build())
                        .setBaseRequest(Dto.BaseRequest.newBuilder()
                                .setAesKey(ByteString.copyFrom(loginCheck.originalAesKey))
                                .setClientVersion(Constant.CLIENT_VERSION)
                                .setDeviceId(ByteString.copyFrom(loginCheck.iPad.getDeviceId()))
                                .setUin(0)
                                .setOsVersion(Constant.OS_VERSION)
                                .setScene(0)
                                .build())
                        .setQrCodeKey(loginCheck.qrCodeKey)
                        .setTimestamp(CommonUtil.now())
                        .setUnknown5(0)
                        .build();
    }

    Teacher createOrUpdateEntity(Dto.UserInfo userInfo) {
        return transactionTemplate.execute((status) -> {
            String wxid = userInfo.getWxid();
            wechatUserDao.lock(wxid);
            WeChatUser weChatUser = wechatUserDao.findOne(wxid);
            if (weChatUser == null) {
                weChatUser = new WeChatUser(wxid, userInfo.getHeadImgUrl(), userInfo.getNickname());
                wechatUserDao.save(weChatUser);
            } else {
                weChatUser.setAvatarUrlPath(userInfo.getHeadImgUrl());
                weChatUser.setNickName(userInfo.getNickname());
                weChatUser.setUpdateTime(DateTimeUtils.now());
                wechatUserDao.save(weChatUser);
            }
            Teacher teacher = teacherDao.findOne(wxid);
            if (teacher == null) {
                teacher = new Teacher(wxid);
                teacherDao.save(teacher);
            }
            return teacher;
        });
    }

    private void ensureUnique(WeChatClient client) {
        for (WeChatClient temp : loginClients) {
            if (Objects.equal(temp.getWxid(), client.getWxid())) {
                temp.shutdown();
            }
        }
        loginClients.add(client);
    }

    private byte[] QRCodePack(byte[] request) {
        int originLength = request.length;
        request = CommonUtil.rsaEncrypt(request);
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeByte(0xbf);
        dataOutput.writeByte(0);
        dataOutput.writeByte((0x1 << 4) | 0xf);
        dataOutput.writeInt(Constant.CLIENT_VERSION);
        dataOutput.writeInt(0);
        dataOutput.write(BaseEncoding.base16().decode("7742080D000000002D21BAA52D4600"));
        dataOutput.write(CommonUtil.varInt(502));
        dataOutput.write(CommonUtil.varInt(originLength));
        dataOutput.write(CommonUtil.varInt(originLength));
        dataOutput.writeByte(Constant.KEY_VERSION);
        dataOutput.write(BaseEncoding.base16().decode("010D00098778"));
        byte[] bytes = dataOutput.toByteArray();
        bytes[1] = (byte) ((bytes.length << 2) | 2);
        return Bytes.concat(bytes, request);
    }

}