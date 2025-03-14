package com.liboshuai.slr.server.biz.util.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class JasyptUtilTest {

    @Test
    void testEncrypt() {
        String content = "xxxxx";
        String encrypted = JasyptUtil.encrypt(content);
        log.info("加密后得到的密文: {}", encrypted);
    }

    @Test
    void testDecrypt() {
        String content = "xxxxx";
        String decrypted = JasyptUtil.decrypt(content);
        log.info("解密后得到的明文: {}", decrypted);
    }
}