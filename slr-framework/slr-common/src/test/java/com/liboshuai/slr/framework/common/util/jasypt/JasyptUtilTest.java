package com.liboshuai.slr.framework.common.util.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class JasyptUtilTest {
    @Test
    void testEncrypt() {
        String content = "Dawn@Imp0513";
        String encrypted = JasyptUtil.encrypt(content);
        log.info("加密后得到的密文: {}", encrypted);
    }

    @Test
    void testDecrypt() {
        String content = "mJP/B42LHiAeULegv4LAy1wh3RxdWv32";
        String decrypted = JasyptUtil.decrypt(content);
        log.info("解密后得到的明文: {}", decrypted);
    }
}