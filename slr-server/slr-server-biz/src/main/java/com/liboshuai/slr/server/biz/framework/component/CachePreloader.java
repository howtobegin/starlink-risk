//package com.liboshuai.slr.server.biz.framework.component;
//
//import com.liboshuai.slr.server.biz.service.riskRule.RuleTargetService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//
//@Slf4j
//@Component
//public class CachePreloader implements ApplicationRunner {
//    @Resource
//    private RuleTargetService ruleTargetService;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        log.info("风控规则目标详情列表数据-缓存预热开始...");
//        try {
//            List<RuleTargetApiDTO> ruleTargetApiDTOList = ruleTargetService.putCacheDetailList();
//            log.info("风控规则目标详情列表数据-缓存预热完成。加载了 {} 条数据到缓存中。", ruleTargetApiDTOList.size());
//        } catch (Exception e) {
//            log.error("风控规则目标详情列表数据-缓存预热失败：", e);
//            // 根据需要决定是否阻止应用启动
//            throw e;
//        }
//    }
//}
