package org.example.zzudate.dto;

import lombok.Data;

@Data
public class UserSoulInfoDto {
    private Long userId;
     //40道题的答案，前端以JSON字符串格式传来{"1":"A", "2":"C", ... "40":"B"}
    private String answers;
}
