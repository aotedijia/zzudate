package org.example.zzudate;

import java.util.Random;

public class GenerateEmailCode{
    public static String generateEmailcode(){
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        // 生成6位数字验证码
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10)); // 随机数字 0-9
        }
        return code.toString();
    }
}