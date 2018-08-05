package com.wumii.wechat.util;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.protobuf.Int32Value;
import org.apache.http.client.fluent.Request;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CommonUtil {

    private static final List<String> keyItems =
            Arrays.asList("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split(""));
    private static final Random random = new Random();
    private static final int MAX_ENCRYPT_BLOCK = 117;

    private CommonUtil() {}

    public static byte[] randomAesKey() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 16) {
            sb.append(keyItems.get(random.nextInt(keyItems.size())));
        }
        return sb.toString().getBytes();
    }

    public static int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static byte[] varInt(int num) {
        byte[] bytes = Int32Value.newBuilder().setValue(num).build().toByteArray();
        return Arrays.copyOfRange(bytes, 1, bytes.length);
    }

    public static byte[] sendRequest(String url, byte[] data) throws IOException {
        return Request.Post(url)
                .bodyByteArray(data)
                .execute().returnContent().asBytes();
    }

    public static byte[] httpGet(String url) throws IOException {
        return Request.Get(url)
                .execute().returnContent().asBytes();
    }

    public static String format(String xml, Map<String, String> args) {
        for (Map.Entry<String, String> entry : args.entrySet()) {
            xml = xml.replace("{" + entry.getKey()  + "}", entry.getValue());
        }
        return xml;
    }

    public static UnpackResponse unpack(byte[] data, byte[] aesKey) {
        Preconditions.checkArgument(data.length > 0x20, "protocol wrong: " +
                BaseEncoding.base16().encode(data));

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        int headerLengthAndCompress = dataInput.readUnsignedByte();
        if (headerLengthAndCompress == 0xbf) {
            headerLengthAndCompress = dataInput.readUnsignedByte();
        }
        int headerLength = headerLengthAndCompress >> 2;
        boolean compress = (headerLengthAndCompress & 0x3) == 1;
        int cookieLength = dataInput.readUnsignedByte() & 0xf;
        dataInput.readInt();
        int uin = dataInput.readInt();
        byte[] cookie = new byte[cookieLength];
        dataInput.readFully(cookie);
        byte[] body = Arrays.copyOfRange(data, headerLength, data.length);
        body = aesDecrypt(body, aesKey);
        if (compress) {
            body = decompress(body);
        }
        return new UnpackResponse(uin, cookie, body);
    }

    public static byte[] compress(byte[] data) {
        Deflater compress = new Deflater();
        compress.setInput(data);
        compress.finish();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!compress.finished()) {
            int i = compress.deflate(buf);
            output.write(buf, 0, i);
        }
        compress.end();
        return output.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        Inflater decompress = new Inflater();
        decompress.setInput(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            while (!decompress.finished()) {
                int i = decompress.inflate(buf);
                output.write(buf, 0, i);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException("decompress error", e);
        }
        decompress.end();
        return output.toByteArray();
    }

    public static class UnpackResponse {
        private int uin;
        private byte[] cookie;
        private byte[] body;

        private UnpackResponse(int uin, byte[] cookie, byte[] body) {
            this.uin = uin;
            this.cookie = cookie;
            this.body = body;
        }

        public int getUin() {
            return uin;
        }

        public byte[] getCookie() {
            return cookie;
        }

        public byte[] getBody() {
            return body;
        }
    }

    public static byte[] aesEncrypt(byte[] data, byte[] aesKey) {
        try {
            SecretKey secretKey = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(aesKey));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            throw new RuntimeException("encrypt error", e);
        }
    }

    public static byte[] aesDecrypt(byte[] data,  byte[] aesKey) {
        try {
            SecretKey secretKey = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(aesKey));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            throw new RuntimeException("decrypt error", e);
        }
    }

    public static byte[] rsaEncrypt(byte[] data) {
        try {
            PublicKey publicKey = getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            return out.toByteArray();
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                | BadPaddingException | InvalidKeySpecException | IllegalBlockSizeException e) {
            throw new RuntimeException("encrypt error", e);
        }
    }

    private static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(Constant.KEY_N, 16),
                new BigInteger(Constant.KEY_E, 16));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

}
