package com.wumii.wechat.service;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.wumii.wechat.dao.IPadDao;
import com.wumii.wechat.dao.TeacherDao;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.IPad;
import com.wumii.wechat.entity.Teacher;
import com.wumii.wechat.exception.WeChatOperationException;
import com.wumii.wechat.util.CommonUtil;
import com.wumii.wechat.util.Constant;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WeChatClient extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(WeChatClient.class);

    private static final int LOGIN = 253;
    private static final int AUTO_LOGIN = 254;
    private static final int HEARTBEAT = 6;
    private static final int PUSH = 24;
    private static final int RECEIVE_ACK = 1000000190;

    private static final int HEARTBEAT_TIMEOUT = 60000;
    private static final int TIMELINE_TIMEOUT = 300000;

    private String wxid;
    private String password;

    private IPadDao iPadDao;
    private TeacherDao teacherDao;
    private DNSService dnsService;

    private WeChatSyncService weChatSyncService;
    private WeChatFriendService weChatFriendService;
    private WeChatSnsTimeLineService weChatSnsTimeLineService;

    private Teacher teacher; //与当前线程绑定的实体类, 需保证数据一致性
    private IPad ipad;

    private Random random = new Random();
    private BlockingQueue<TaskWrapper> event = new LinkedBlockingQueue<>();
    private ExecutorService listenExecutor = Executors.newSingleThreadExecutor();

    private byte[] aesKey;
    private byte[] publicKeyData;
    private BCECPublicKey publicKey;
    private BCECPrivateKey privateKey;

    private boolean login = false;
    private int packSeq = 1;
    private long lastHeartbeat = 0;
    private long lastRefreshTimeLine = 0;

    private volatile byte[] sessionKey;
    private volatile int uin;
    private volatile byte[] cookie;
    private volatile boolean running = true;

    private Socket socket;

    WeChatClient(ApplicationContext context, IPad ipad, Teacher teacher, String password, byte[] aesKey) {
        this.ipad = ipad;
        this.wxid = teacher.getWxid();
        this.password = password;
        this.teacher = teacher;
        this.aesKey = aesKey;
        this.iPadDao = context.getBean(IPadDao.class);
        this.teacherDao = context.getBean(TeacherDao.class);
        this.dnsService = context.getBean(DNSService.class);
        this.weChatSyncService = context.getBean(WeChatSyncService.class);
        this.weChatFriendService = context.getBean(WeChatFriendService.class);
        this.weChatSnsTimeLineService = context.getBean(WeChatSnsTimeLineService.class);
    }

    WeChatClient(ApplicationContext context, IPad ipad, Teacher teacher) {
        this.ipad = ipad;
        this.wxid = teacher.getWxid();
        this.teacher = teacher;
        this.sessionKey = teacher.getSessionKey();
        this.aesKey = this.sessionKey;
        this.iPadDao = context.getBean(IPadDao.class);
        this.teacherDao = context.getBean(TeacherDao.class);
        this.dnsService = context.getBean(DNSService.class);
        this.weChatSyncService = context.getBean(WeChatSyncService.class);
        this.weChatFriendService = context.getBean(WeChatFriendService.class);
        this.weChatSnsTimeLineService = context.getBean(WeChatSnsTimeLineService.class);
    }

    public boolean inEventLoop() {
        return Thread.currentThread() == this;
    }

    public String getWxid() {
        return wxid;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public int getUin() {
        return uin;
    }

    public IPad getIpad() {
        return ipad;
    }

    public boolean isRunning() {
        return running;
    }

    public byte[] sendRequest(String cgi, byte[] request, int cgiType, boolean compress) throws IOException {
        logger.info("send http request to {}, body: {}", cgi, BaseEncoding.base16().encode(request));
        CommonUtil.UnpackResponse unpack = CommonUtil.unpack(
                CommonUtil.sendRequest(
                        "http://" + dnsService.getShortConnectionIP() + cgi,
                        pack(request, cgiType, compress)), sessionKey);
        logger.info("receive http response, body: {}", BaseEncoding.base16().encode(unpack.getBody()));
        uin = unpack.getUin();
        cookie = unpack.getCookie();
        updateTeacher();
        return unpack.getBody();
    }

    @Override
    public void run() {
        try {
            init();
            login();
            listen(); //启动监听
            eventLoop(); //事件循环
        } catch (Throwable e) {
            logger.error("wechat client shutdown with error", e);
            shutdown();
        }
    }

    TaskFuture submit(Task task) {
        CountDownLatch latch = new CountDownLatch(1);
        TaskFuture taskFuture = new TaskFuture(latch);
        TaskWrapper taskWrapper = new TaskWrapper();
        taskWrapper.task = task;
        taskWrapper.future = taskFuture;
        event.offer(taskWrapper);
        return taskFuture;
    }

    private class TaskWrapper {
        private Task task;
        private TaskFuture future;
    }

    public static class TaskFuture<V> implements Future<V> {

        private CountDownLatch latch;
        private volatile boolean success;

        public TaskFuture(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDone() {
            return latch.getCount() == 0;
        }

        @Override
        public V get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        public boolean isSuccess() throws InterruptedException {
            latch.await();
            return success;
        }

        public void setSuccess(boolean success) {
            latch.countDown();
            this.success = success;
        }
    }

    private void eventLoop() throws Exception {
        while (running) {
            tryHeartbeat();
            tryRefreshTimeLine();
            executeOneEvent();
            Thread.sleep(random.nextInt(3000) + 2000);
        }
    }

    private void executeOneEvent() throws Exception {
        TaskWrapper taskWrapper = event.poll();
        if (taskWrapper != null) {
            try {
                taskWrapper.task.run();
                taskWrapper.future.setSuccess(true);
            } catch (WeChatOperationException e) {
                logger.error(e.getMessage());
                taskWrapper.future.setSuccess(false);
            }
        }
    }

    private void listen() {
        listenExecutor.submit(() -> {
            while (running) {
                try {
                    ByteArrayDataInput header = ByteStreams.newDataInput(readByte(16));
                    int bodyLength = header.readInt() - 16;
                    header.readInt();
                    int cmdIdAck = header.readInt();
                    int seqAck = header.readInt();
                    byte[] body = readByte(bodyLength);
                    logger.info("receive data from wechat {}", BaseEncoding.base16().encode(body));
                    if (cmdIdAck == PUSH && seqAck == 0) {
                        if (teacher.getSyncKey() == null) {
                            submit(() -> {
                                weChatSyncService.weChatInit(this);
                                weChatFriendService.getContact(this);
                            });
                        } else {
                            submit(() -> {
                                byte[] reply = weChatSyncService.sync(this);
                                socket.getOutputStream().write(longConnPack(RECEIVE_ACK, reply));
                            });
                        }
                    } else {
                        int cmdId = cmdIdAck - 1000000000;
                        if (cmdId == LOGIN || cmdId == AUTO_LOGIN) {
                            CommonUtil.UnpackResponse unpack = CommonUtil.unpack(body, aesKey);
                            Dto.LoginResponse loginResponse = Dto.LoginResponse
                                    .parseFrom(unpack.getBody());
                            ByteString ecdhKey = loginResponse.getSession().getEcdh().getEcdhKey().getKey();
                            ByteString encryptedKey = loginResponse.getSession().getSessionKey().getEncryptedKey();
                            uin = unpack.getUin();
                            cookie = unpack.getCookie();
                            sessionKey = parseKey(ecdhKey.toByteArray(), encryptedKey.toByteArray());

                            login = true;
                            teacher.setSessionKey(sessionKey);
                            teacher.setAutoLoginKey(loginResponse.getSession().getAutoLoginKey().toByteArray());
                            updateTeacher();
                            logger.info("user: {} login success", wxid);
                        }
                    }
                } catch (Throwable e) {
                    logger.error("listen error", e);
                    shutdown();
                }
            }
        });
    }

    private void updateTeacher() {
        teacher.setUin(uin);
        teacher.setCookie(cookie);
        teacherDao.save(teacher);
    }

    private byte[] parseKey(byte[] ecdhKey, byte[] encryptedKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(getECPublicKey(ecdhKey), true);
            byte[] secret = keyAgreement.generateSecret();
            secret = DigestUtils.md5(secret);
            return CommonUtil.aesDecrypt(encryptedKey, secret);
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidKeyException | InvalidKeySpecException e) {
            throw new RuntimeException("key agreement error", e);
        }
    }

    private ECPublicKey getECPublicKey(byte[] remotePubKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException {
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(remotePubKey, 1, 29));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(remotePubKey, 29, 57));
        ECPublicKeySpec otherKeySpec = new ECPublicKeySpec(new ECPoint(x, y), publicKey.getParams());
        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
        return (ECPublicKey) keyFactory.generatePublic(otherKeySpec);
    }

    private byte[] readByte(int expected) throws IOException {
        if (expected <= 0) {
            return new byte[0];
        }
        int read = 0;
        byte[] buffer = new byte[expected];
        do {
            int len = socket.getInputStream().read(buffer, read, expected - read);
            if (len == -1) {
                throw new IOException("connection closed by wechat");
            }
            read += len;
        } while (read < expected);
        return buffer;
    }

    private void firstLogin() throws IOException {
        int now = CommonUtil.now();
        String uuid = UUID.randomUUID().toString();
        Dto.UserLoginRequest userLoginRequest = buildUserLoginRequest();
        Dto.DeviceInfo deviceInfo = buildDeviceInfo(now, uuid);
        byte[] loginPack = firstLoginPack(userLoginRequest, deviceInfo);
        byte[] longConnPack = longConnPack(LOGIN, loginPack);
        socket.getOutputStream().write(longConnPack);
    }

    private Dto.DeviceInfo buildDeviceInfo(int now, String uuid) {
        return Dto.DeviceInfo.newBuilder()
                    .setBaseRequest(Dto.BaseRequest.newBuilder()
                            .setAesKey(ByteString.EMPTY)
                            .setClientVersion(Constant.CLIENT_VERSION)
                            .setDeviceId(ByteString.copyFrom(ipad.getDeviceId()))
                            .setUin(0)
                            .setOsVersion(Constant.OS_VERSION)
                            .setScene(1)
                            .build())
                    .setUnknown2(Dto.DeviceInfo.Unknown2.newBuilder().setUnknown7(ByteString.EMPTY).build())
                    .setImei(ipad.getImei())
                    .setSoftInfoXml(String.format("<softtype>"
                            + "<k3>9.3.5</k3>"
                            + "<k9>iPad</k9>"
                            + "<k10>2</k10>"
                            + "<k19>872AADB1-A19A-46A4-B478-0753749512A0</k19>"
                            + "<k20>%s</k20>"
                            + "<k21>WM-Office</k21>"
                            + "<k22>(null)</k22>"
                            + "<k24>%s</k24>"
                            + "<k33>微信</k33>"
                            + "<k47>1</k47>"
                            + "<k50>1</k50>"
                            + "<k51>com.tencent.xin</k51>"
                            + "<k54>iPad3,1</k54>"
                            + "<k61>2</k61>"
                            + "</softtype>", uuid, ipad.getMac()))
                    .setUnknown5(0)
                    .setImeiAndTimestamp(ipad.getImei() + "-" + now)
                    .setLoginDeviceName("iPad")
                    .setLoginDevice("iPad")
                    .setLanguage(Constant.LANGUAGE)
                    .setUnknown11(Dto.DeviceInfo.Unknown11.newBuilder().setUnknown6(48).setUnknown7(46).build())
                    .setUnknown13(0)
                    .setTimestamp(now)
                    .setDeviceBrand("Apple")
                    .setRealCountry("CN")
                    .setPackageName("com.tencent.xin")
                    .setUuid(uuid)
                    .setDeviceModel("iPad3,1")
                    .setUnknown22(2)
                    .build();
    }

    private Dto.UserLoginRequest buildUserLoginRequest() {
        return Dto.UserLoginRequest.newBuilder()
                    .setAesKey(Dto.AesKey.newBuilder().setSize(16).setKey(ByteString.copyFrom(aesKey)).build())
                    .setWxid(wxid)
                    .setEncryptedPassword(password)
                    .setEcdh(Dto.Ecdh.newBuilder()
                            .setNid(713)
                            .setEcdhKey(Dto.Ecdh.EcdhKey.newBuilder()
                                    .setSize(publicKeyData.length)
                                    .setKey(ByteString.copyFrom(publicKeyData))
                                    .build())
                            .build())
                    .build();
    }

    private void autoLogin() throws IOException {
        Dto.AutoLoginRequest autoLoginRequest = buildAutoLoginRequest();
        Dto.AutoLoginDeviceInfo autoLoginDeviceInfo = buildAutoLoginDeviceInfo();
        byte[] loginPack = autoLoginPack(autoLoginRequest, autoLoginDeviceInfo);
        byte[] longConnPack = longConnPack(AUTO_LOGIN, loginPack);
        socket.getOutputStream().write(longConnPack);
    }

    private Dto.AutoLoginDeviceInfo buildAutoLoginDeviceInfo() {
        return Dto.AutoLoginDeviceInfo.newBuilder()
                    .setBaseRequest(Dto.BaseRequest.newBuilder()
                            .setAesKey(ByteString.EMPTY)
                            .setClientVersion(Constant.CLIENT_VERSION)
                            .setDeviceId(ByteString.copyFrom(ipad.getDeviceId()))
                            .setUin(teacher.getUin())
                            .setOsVersion(Constant.OS_VERSION)
                            .setScene(2)
                            .build())
                    .setUnknown2(Dto.AutoLoginDeviceInfo.Unknown2.newBuilder()
                            .setUnknown1(Dto.AutoLoginDeviceInfo.Unknown2.Unknown1.newBuilder()
                                    .setUnknown1(0)
                                    .setUnknown2(ByteString.EMPTY)
                                    .build())
                            .build())
                    .setAutoLoginKey(ByteString.copyFrom(teacher.getAutoLoginKey()))
                    .setImei(ipad.getImei())
                    .setSoftInfoXml(String.format("<softtype>"
                            + "<k3>9.3.5</k3>"
                            + "<k9>iPad</k9>"
                            + "<k10>2</k10>"
                            + "<k19>6F75C2ED-C3D0-4425-971C-CE1ED469E608</k19>"
                            + "<k20>%s</k20>"
                            + "<k21>WM-Office</k21>"
                            + "<k22>(null)</k22>"
                            + "<k24>%s</k24>"
                            + "<k33>微信</k33>"
                            + "<k47>1</k47>"
                            + "<k50>1</k50>"
                            + "<k51>com.tencent.xin</k51>"
                            + "<k54>iPad3,1</k54>"
                            + "</softtype>", UUID.randomUUID().toString().toUpperCase(), ipad.getMac()))
                    .setUnknown6(0)
                    .setImeiAndTimestamp(ipad.getImei() + "-" + CommonUtil.now())
                    .setLoginDeviceName("iPad")
                    .setLoginDevice("iPad")
                    .setLanguage(Constant.LANGUAGE)
                    .setUnknown12(Dto.AutoLoginDeviceInfo.Unknown12.newBuilder()
                            .setUnknown6(48)
                            .setUnknown7(46)
                            .build())
                    .build();
    }

    private Dto.AutoLoginRequest buildAutoLoginRequest() {
        return Dto.AutoLoginRequest.newBuilder()
                    .setAesKey(Dto.AesKey.newBuilder().setSize(16).setKey(ByteString.copyFrom(aesKey)).build())
                    .setEcdh(Dto.Ecdh.newBuilder()
                            .setNid(713)
                            .setEcdhKey(Dto.Ecdh.EcdhKey.newBuilder()
                                    .setSize(publicKeyData.length)
                                    .setKey(ByteString.copyFrom(publicKeyData))
                                    .build())
                            .build())
                    .build();
    }

    private void login() throws IOException {
        if (sessionKey == null) {
            firstLogin();
        } else {
            autoLogin();
        }
    }

    private void init() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(dnsService.getLongConnectionIP(), 443));
        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp224r1");
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = (BCECPublicKey) keyPair.getPublic();
            privateKey = (BCECPrivateKey) keyPair.getPrivate();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(
                    ASN1Primitive.fromByteArray(publicKey.getEncoded()));
            publicKeyData = publicKeyInfo.getPublicKeyData().getBytes();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("gen key pair error", e);
        }
    }

    private void tryHeartbeat() throws IOException {
        long now = System.currentTimeMillis();
        if (now - lastHeartbeat > HEARTBEAT_TIMEOUT) {
            logger.debug("send heart beat");
            socket.getOutputStream().write(longConnPack(HEARTBEAT, new byte[0]));
            lastHeartbeat = now;
        }
    }

    private void tryRefreshTimeLine() {
        long now = System.currentTimeMillis();
        if (login && now - lastRefreshTimeLine > TIMELINE_TIMEOUT) {
            submit(() -> weChatSnsTimeLineService.updateSnsTimeLine(this));
            lastRefreshTimeLine = now;
        }
    }

    private byte[] firstLoginPack(Dto.UserLoginRequest userLoginRequest, Dto.DeviceInfo deviceInfo) {
        byte[] user = userLoginRequest.toByteArray();
        byte[] device = deviceInfo.toByteArray();

        byte[] body1 = CommonUtil.rsaEncrypt(CommonUtil.compress(user));
        byte[] body2 = CommonUtil.aesEncrypt(CommonUtil.compress(device), aesKey);

        ByteArrayDataOutput subHeader = ByteStreams.newDataOutput();
        subHeader.writeInt(user.length);
        subHeader.writeInt(device.length);
        subHeader.writeInt(body1.length);
        byte[] body = Bytes.concat(subHeader.toByteArray(), body1, body2);

        ByteArrayDataOutput header = ByteStreams.newDataOutput();
        header.writeByte(0);
        header.writeByte((0x7 << 4) | 0xf);
        header.writeInt(Constant.CLIENT_VERSION);
        header.write(new byte[4]);
        header.write(new byte[15]);
        header.write(CommonUtil.varInt(701));
        header.write(CommonUtil.varInt(body.length));
        header.write(CommonUtil.varInt(body.length));
        header.writeByte(Constant.KEY_VERSION);
        header.writeByte(0x01);
        header.writeByte(0x02);
        byte[] headerBytes = header.toByteArray();
        headerBytes[0] = (byte) ((headerBytes.length << 2) | 2);
        return Bytes.concat(headerBytes, body);
    }

    private byte[] autoLoginPack(Dto.AutoLoginRequest autoLoginRequest, Dto.AutoLoginDeviceInfo autoLoginDeviceInfo) {
        byte[] user = autoLoginRequest.toByteArray();
        byte[] device = autoLoginDeviceInfo.toByteArray();

        byte[] body1 = CommonUtil.rsaEncrypt(CommonUtil.compress(user));
        byte[] body2 = CommonUtil.aesEncrypt(CommonUtil.compress(user), aesKey);
        byte[] body3 = CommonUtil.aesEncrypt(CommonUtil.compress(device), aesKey);

        ByteArrayDataOutput subHeader = ByteStreams.newDataOutput();
        subHeader.writeInt(user.length);
        subHeader.writeInt(device.length);
        subHeader.writeInt(body1.length);
        subHeader.writeInt(body2.length);
        byte[] body = Bytes.concat(subHeader.toByteArray(), body1, body2, body3);

        ByteArrayDataOutput header = ByteStreams.newDataOutput();
        header.writeByte(0xbf);
        header.writeByte(0);
        header.writeByte((0x9 << 4) | 0xf);
        header.writeInt(Constant.CLIENT_VERSION);
        header.writeInt(teacher.getUin());
        header.write(teacher.getCookie());
        header.write(CommonUtil.varInt(702));
        header.write(CommonUtil.varInt(body.length));
        header.write(CommonUtil.varInt(body.length));
        header.writeByte(Constant.KEY_VERSION);
        header.write(BaseEncoding.base16().decode("010D000995E0F39105"));
        byte[] headerBytes = header.toByteArray();
        headerBytes[1] = (byte) ((headerBytes.length << 2) | 2);
        return Bytes.concat(headerBytes, body);
    }

    private byte[] longConnPack(int cmdId, byte[] data) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeInt(data.length + 16);
        dataOutput.writeByte(0x00);
        dataOutput.writeByte(0x10);
        dataOutput.writeByte(0x00);
        dataOutput.writeByte(0x01);
        dataOutput.writeInt(cmdId);
        if (cmdId == HEARTBEAT) {
            dataOutput.writeInt(0xFFFFFFFF);
        } else {
            dataOutput.writeInt(packSeq++);
        }
        dataOutput.write(data);
        return dataOutput.toByteArray();
    }

    private byte[] pack(byte[] data, int cgiType, boolean compress) {
        int originLength = data.length;
        int zlibLength = data.length;
        if (compress) {
            data = CommonUtil.compress(data);
            zlibLength = data.length;
        }
        data = CommonUtil.aesEncrypt(data, sessionKey);

        ByteArrayDataOutput header = ByteStreams.newDataOutput();
        header.writeByte(0xbf);
        header.writeByte(0);
        header.writeByte((0x5 << 4) | 0xf);
        header.writeInt(Constant.CLIENT_VERSION);
        header.writeInt(uin);
        header.write(cookie);
        header.write(CommonUtil.varInt(cgiType));
        header.write(CommonUtil.varInt(originLength));
        header.write(CommonUtil.varInt(zlibLength));
        header.write(new byte[15]);
        byte[] headerBytes = header.toByteArray();
        headerBytes[1] = (byte) ((headerBytes.length << 2) | (compress ? 1 : 2));
        return Bytes.concat(headerBytes, data);
    }

    public synchronized void shutdown() {
        if (!running) {
            return;
        }
        running = false;
        ipad.setOccupied(false);
        iPadDao.save(ipad);
        listenExecutor.shutdown();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("shutdown wechat client error", e);
        }
        logger.info("wechat client {} shutdown success", wxid);
    }
}
