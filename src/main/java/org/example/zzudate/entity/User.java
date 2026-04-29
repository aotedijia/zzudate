package org.example.zzudate.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User{
    private String id;
    private String name;
    private String email;//邮箱
    private LocalDateTime createTime;//注册时间
    private String number;//联系方式
    private Boolean gender;//0为女，1为男
    private String height;//身高
    private String college;//学院
    private String campus;//校区-主校区-北校区-南校区-东校区
    private Integer grade;//年级-1本科生2硕士研究生3博士研究生
    private String answers;//前端传进来答案转化的JSON字符串
    private String choose;//选择倾向，0为女，1为男
}