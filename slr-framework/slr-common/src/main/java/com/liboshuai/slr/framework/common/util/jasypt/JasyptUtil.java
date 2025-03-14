package com.liboshuai.slr.framework.common.util.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

@Slf4j
public class JasyptUtil {

    private static final String ALGORITHM = "PBEWithMD5AndDES";
    // https://juejin.cn/post/7481279088620568613
    private static final String PASSWORD = "$ozNW6^3Bp@Z8Gxc";

    /**
     * 加密
     *
     * @param plaintext 明文密码     * @return
     */
    public static String encrypt(String plaintext) {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        // 算法类型
        config.setAlgorithm(ALGORITHM);
        //生成秘钥的公钥
        config.setPassword(PASSWORD);
        //应用配置
        encryptor.setConfig(config);
        //加密
        return encryptor.encrypt(plaintext);
    }

    /**
     * 解密
     *
     * @param ciphertext 待解密秘钥
     */
    public static String decrypt(String ciphertext) {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(ALGORITHM);
        //生成秘钥的公钥
        config.setPassword(PASSWORD);
        //应用配置
        encryptor.setConfig(config);
        //解密
        return encryptor.decrypt(ciphertext.replace("LBS(", "").replace(")", ""));
    }

}
