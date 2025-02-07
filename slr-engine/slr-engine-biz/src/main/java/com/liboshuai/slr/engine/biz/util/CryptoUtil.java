package com.liboshuai.slr.engine.biz.util;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @Author liboshuai
 * @Date 2023/11/9 15:13
 */
public class CryptoUtil {
    /**
     * 密钥
     */
    private static final String SECRET_KEY = "th0XVRN49kG2TalGgwYbdw==";
    /**
     * 算法
     */
    private static final String AES = "AES";

    public static String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e);
        }
    }

    public static String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e);
        }
    }

    /**
     * 生成 AES 密钥
     */
    public static String generateKey() {
        SecureRandom secureRandom = new SecureRandom(); // SecureRandom 实例
        byte[] key = new byte[16]; // 创建一个空的 16 字节的字节数组
        secureRandom.nextBytes(key); // 生成随机字节并将其放入字节数组
        return Base64.getEncoder().encodeToString(key); // 将字节数组编码为 Base64 字符串以便打印和查看
    }
}
