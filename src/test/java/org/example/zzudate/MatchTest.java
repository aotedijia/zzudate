package org.example.zzudate;

import org.example.zzudate.entity.MatchResult;
import org.example.zzudate.mapper.MatchResultMapper;
import org.example.zzudate.service.WeeklyMatchService;
import org.junit.jupiter.api.Test; // 必须使用 JUnit 5 的注解
import org.springframework.boot.test.context.SpringBootTest; // 启动容器关键指标
import jakarta.annotation.Resource;
import java.util.List;

@SpringBootTest // 1. 必须使用此注解，否则 @Resource 注入的对象全是 null
public class MatchTest {

    @Resource
    private WeeklyMatchService weeklyMatchService;

    @Resource
    private MatchResultMapper matchResultMapper;

    @Test // 2. 确保导入的是 org.junit.jupiter.api.Test
    public void testSoulMatch() {
        System.out.println(">>> 正在启动灵魂匹配测试指标分析...");

        // 执行匹配逻辑
        weeklyMatchService.runWeeklySoulMatch();

        // 验证数据库写入指标
        List<MatchResult> results = matchResultMapper.selectList(null);

        System.out.println("--- 匹配结果指标报告 ---");
        if (results.isEmpty()) {
            System.err.println("[异常] 未生成任何匹配结果。原因可能：1.用户answers字段为空 2.池中用户少于2人");
        } else {
            for (MatchResult mr : results) {
                System.out.println("甲方: " + mr.getUserNameA() + " | 乙方: " + mr.getUserNameB());
                System.out.println("契合度得分: " + mr.getScore() + "%");
                System.out.println("匹配文案: " + mr.getDescription());
                System.out.println("联系方式同步: A-" + mr.getNumberA() + " | B-" + mr.getNumberB());
            }
        }
        System.out.println("-----------------------");
    }
}