package service; // 业务层包名

import org.springframework.stereotype.Service; // Spring 服务注解
import com.alibaba.fastjson.JSON; // 阿里 JSON 工具
import com.alibaba.fastjson.TypeReference; // 泛型解析工具
import java.util.Map; // Map 接口
import java.util.HashMap; // Map 实现

@Service // 标记为 Spring 管理的服务
public class Match { // 灵魂匹配核心算法类

    /**
     * 【重载方法】供全量匹配使用：直接传入已解析好的 Map，极致节省 CPU
     */
    public double calculateMatch(Map<Integer, String> mapA, Map<Integer, String> mapB) { // 计算方法
        if (mapA == null || mapB == null || mapA.isEmpty() || mapB.isEmpty()) return 0.0; // 判空逻辑

        double totalScore = 0.0; // 初始化总分

        for (int i = 1; i <= 40; i++) { // 遍历 40 道灵魂题目
            String valA = mapA.get(i); // 获取 A 的选项
            String valB = mapB.get(i); // 获取 B 的选项

            if (valA != null && valA.equals(valB)) { // 如果选项一致
                totalScore += getWeight(i); // 按照预设权重加分
            }
        }
        return totalScore; // 返回计算结果
    }

    /**
     * 【原方法】供单次查询或测试使用
     */
    public double calculateMatch(String answersA, String answersB) { // 接收 JSON 字符串的方法
        return calculateMatch(parseJson(answersA), parseJson(answersB)); // 解析后转调 Map 计算方法
    }

    public Map<Integer, String> parseJson(String jsonStr) { // JSON 解析工具方法
        try { // 异常处理
            if (jsonStr == null || jsonStr.isEmpty()) return new HashMap<>(); // 空字符串处理
            return JSON.parseObject(jsonStr, new TypeReference<Map<Integer, String>>() {}); // 解析画像
        } catch (Exception e) { // 捕获解析异常
            System.err.println(">>> [解析异常] 灵魂画像 JSON 格式非法: " + e.getMessage()); // 输出错误日志
            return new HashMap<>(); // 返回空 Map
        }
    }

    private double getWeight(int id) { // 权重分配逻辑
        if (id >= 1 && id <= 8) return 0.025; // 物质底色 (20%)
        if (id >= 9 && id <= 18) return 0.025; // 精神依恋 (25%)
        if (id >= 19 && id <= 28) return 0.015; // 生活节律 (15%)
        if (id >= 29 && id <= 40) return 0.0333; // 灵魂底线 (40%)
        return 0.0; // 兜底
    }
}