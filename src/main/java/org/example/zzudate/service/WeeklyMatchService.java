package org.example.zzudate.service; // 定义包名，属于业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson.JSON; // 引入 Fastjson 用于处理 JSON 数据
import com.alibaba.fastjson.TypeReference; // 引入泛型参考，用于解析复杂的 Map 结构
import org.example.zzudate.entity.MatchResult; // 引入匹配结果实体类
import org.example.zzudate.entity.User; // 引入用户实体类
import org.example.zzudate.mapper.MatchResultMapper; // 引入匹配结果数据库操作接口
import org.example.zzudate.mapper.UserMapper; // 引入用户数据库操作接口
import org.springframework.beans.factory.annotation.Autowired; // 引入自动注入注解
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; // 引入服务层注解

import java.util.*; // 引入工具类（List, Map, Set, UUID 等）

@Service // 标记该类为 Spring 管理的 Service 组件
public class WeeklyMatchService { // 每周定时匹配核心业务类

    @Autowired // 自动注入用户 Mapper
    private UserMapper userMapper; // 用户数据库操作实例
    @Autowired // 自动注入匹配结果 Mapper
    private MatchResultMapper matchResultMapper; // 结果数据库操作实例
    @Autowired // 自动注入匹配算法类
    private Match match; // 算法逻辑实例

    /**
     * 执行每周灵魂匹配的核心方法
     * 逻辑：清空旧数据 -> 预存画像 -> 双循环寻找最优解 -> 批量写入
     */
    @Scheduled(cron = "0 0 19 ? * WED")
    public void runWeeklySoulMatch(){ // 全量匹配执行入口
        long startTime = System.currentTimeMillis(); // 记录程序开始执行的时间戳，用于性能分析
        matchResultMapper.delete(null); // 第一步：物理清空旧的匹配记录（阅后即焚，开启新一周）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(User::getAnswers).ne(User::getAnswers, "");
        List<User> userList = userMapper.selectList(queryWrapper); // 第二步：只拉取已填写问卷的用户
        if (userList == null || userList.size() < 2) return; // 如果池子人数不足 2 人，无法配对，直接结束

        Map<String, Map<Integer, String>> portraitCache = new HashMap<>(); // 创建画像缓存 Map，避免循环内重复解析 JSON
        for (User u : userList) { // 遍历所有用户，进行画像预处理
            portraitCache.put(u.getId(), parsePortrait(u.getAnswers())); // 将用户的 JSON 答案解析为题目-选项的 Map 并存入缓存
        }

        List<MatchResult> finalResults = new ArrayList<>(); // 初始化最终匹配成功的结果集合
        Set<String> processedUsers = new HashSet<>(); // 创建已匹配用户集合，确保每个灵魂本周只会被分配给一个人

        // 3. 计算所有合法候选对的分数，存入列表
        List<double[]> candidatePairs = new ArrayList<>(); // 每项: [indexA, indexB, score]
        for (int i = 0; i < userList.size(); i++) {
            User userA = userList.get(i);
            for (int j = i + 1; j < userList.size(); j++) {
                User userB = userList.get(j);
                if (!canMatch(userA, userB)) continue;
                double score = match.calculateMatch(
                        portraitCache.get(userA.getId()),
                        portraitCache.get(userB.getId())
                );
                candidatePairs.add(new double[]{i, j, score});
            }
        }

        // 4. 按分数从高到低排序，全局贪心配对
        candidatePairs.sort((a, b) -> Double.compare(b[2], a[2]));

        for (double[] pair : candidatePairs) {
            User userA = userList.get((int) pair[0]);
            User userB = userList.get((int) pair[1]);
            double score = pair[2];
            if (processedUsers.contains(userA.getId()) || processedUsers.contains(userB.getId())) continue;
            if (score < 0.3) continue; // 低于30%阈值不配对

            MatchResult result = new MatchResult();
            result.setId(UUID.randomUUID().toString());
            result.setUserIdA(userA.getId());
            result.setUserAnswerA(userA.getAnswers());
            result.setUserAnswerB(userB.getAnswers());
            result.setUserIdB(userB.getId());
            result.setUserNameA(userA.getName());
            result.setUserNameB(userB.getName());
            result.setScore(score * 100);
            result.setDescription(generateRealDescription(score * 100, portraitCache.get(userA.getId()), portraitCache.get(userB.getId())));

            finalResults.add(result);
            processedUsers.add(userA.getId());
            processedUsers.add(userB.getId());
        }

        // 5. 批量存入数据库：将本周计算出的所有灵魂火花持久化
        for (MatchResult mr : finalResults) { // 遍历结果集
            matchResultMapper.insert(mr); // 执行 SQL 插入操作
        }

        // 指标分析日志输出 [cite: 2026-01-01]
        analyzeMetrics(startTime, userList.size(), finalResults.size());
    }

    /**
     * 判断两个用户是否可以配对：双向性别倾向 + 同校区
     */
    private boolean canMatch(User userA, User userB) {
        // 同校区校验：校区不同则不可配对（任一方未设置校区则放行）
        if (userA.getCampus() != null && userB.getCampus() != null
                && !userA.getCampus().equals(userB.getCampus())) {
            return false;
        }
        // A的倾向筛选
        if (userA.getChoose() != null) {
            boolean preferMale = "1".equals(userA.getChoose());
            if (!Objects.equals(userB.getGender(), preferMale)) return false;
        } else {
            if (Objects.equals(userA.getGender(), userB.getGender())) return false;
        }
        // B的倾向筛选（双向验证）
        if (userB.getChoose() != null) {
            boolean bPreferMale = "1".equals(userB.getChoose());
            if (!Objects.equals(userA.getGender(), bPreferMale)) return false;
        } else {
            if (Objects.equals(userA.getGender(), userB.getGender())) return false;
        }
        return true;
    }

    /**
     * 将用户数据库中的 JSON 字符串解析为 Java 集合对象
     */
    private Map<Integer, String> parsePortrait(String json) { // 定义解析私有方法
        if (json == null || json.isEmpty()) return new HashMap<>(); // 判空处理，防止解析报错
        return JSON.parseObject(json, new TypeReference<Map<Integer, String>>() {}); // 使用 Fastjson 进行强转
    }

    /**
     * 基于 40 维画像的对位情况，生成真实的匹配描述
     */
    public String generateRealDescription(double totalScore, Map<Integer, String> mapA, Map<Integer, String> mapB) { // 定义描述生成逻辑
        // 1. 统计四个维度的实际匹配题目数
        int materialMatches = countMatches(mapA, mapB, 1, 8); // 物质底色维度匹配数 (Q1-Q8)
        int spiritualMatches = countMatches(mapA, mapB, 9, 18); // 精神依恋维度匹配数 (Q9-Q18)
        int lifestyleMatches = countMatches(mapA, mapB, 19, 28); // 生活节律维度匹配数 (Q19-Q28)
        int soulMatches = countMatches(mapA, mapB, 29, 40); // 灵魂底线维度匹配数 (Q29-Q40)

        // 2. 找到本对匹配中，共鸣感最强（重合题数最多）的维度
        String strongestDimension = "灵魂底线"; // 默认最强维度为灵魂底线
        int max = soulMatches; // 初始化最大值为灵魂维度匹配数
        if (materialMatches > max) { strongestDimension = "物质底色"; max = materialMatches; } // 比较物质维度
        if (spiritualMatches > max) { strongestDimension = "精神依恋"; max = spiritualMatches; } // 比较精神维度
        if (lifestyleMatches > max) { strongestDimension = "生活节律"; max = lifestyleMatches; } // 比较生活维度

        // 3. 根据最终得分的高低和最强维度组合出真诚的文案
        StringBuilder desc = new StringBuilder(); // 使用 StringBuilder 拼接字符串，性能更佳
        desc.append("基于 40 维画像分析，你们的契合度达到 ").append(String.format("%.1f", totalScore)).append("%。"); // 基础分值播报

        if (totalScore >= 60) { // 针对 60 分以上的高契合度用户
            desc.append("这是一场跨越维度的相遇。你们在「").append(strongestDimension).append("」上表现出惊人的一致，"); // 肯定最强维度的共鸣
            desc.append("仿佛早已习惯了彼此的思考方式。"); // 情感引导
        } else { // 针对分值在及格线以上的普通匹配
            desc.append("虽然性格各有千秋，但「").append(strongestDimension).append("」是你们之间最稳固的桥梁，"); // 寻找共振点
            desc.append("这种共鸣让你们在万千灵魂中脱颖而出。"); // 仪式感结语
        }

        // 4. 关键点睛：针对核心三观（Q32 忠诚观）的一致性进行特殊播报
        if (Objects.equals(mapA.get(32), mapB.get(32)) && mapA.get(32) != null) { // 如果双方都填了且答案一样
            desc.append(" 值得一提的是，你们对「忠诚」有着相同的敬畏。"); // 增加安全感与信任背书
        }

        return desc.toString(); // 返回最终生成的描述长文本
    }

    /**
     * 辅助工具：统计指定题号区间内答案完全一致的数量
     */
    private int countMatches(Map<Integer, String> a, Map<Integer, String> b, int start, int end) { // 计数方法
        int count = 0; // 初始化计数器
        for (int i = start; i <= end; i++) { // 循环区间内的题号
            if (Objects.equals(a.get(i), b.get(i)) && a.get(i) != null) count++; // 如果答案一致且不为空，计数器加 1
        }
        return count; // 返回总数
    }

    /**
     * 指标分析与性能监控报告输出 [cite: 2026-01-01]
     */
    private void analyzeMetrics(long startTime, int totalUsers, int matchPairs) { // 定义分析方法
        long duration = System.currentTimeMillis() - startTime; // 计算总耗时
        System.out.println("--- ZZUDate 每周三 19:00 灵魂匹配指标报告 ---");
        System.out.println("[性能] 计算与数据库写入总耗时: " + duration + "ms"); // 监控 2核2G 压力
        System.out.println("[规模] 画像池总数: " + totalUsers + " | 成功配对对数: " + matchPairs); // 监控活跃度
        System.out.println("[真诚] 匹配覆盖率: " + (matchPairs * 2.0 / totalUsers * 100) + "%"); // 分析落单率
        System.out.println("------------------------------------------");
    }
}