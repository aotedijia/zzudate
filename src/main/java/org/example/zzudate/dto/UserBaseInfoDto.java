package org.example.zzudate.dto;

import lombok.Data;


@Data
public class UserBaseInfoDto {
    private String id;
    private String number;//联系方式
    private Boolean gender;//0为女，1为男
    private String height;//身高
    private String college;//学院
    private String campus;//校区-主校区-北校区-南校区-东校区
    private Integer grade;//年级-1本科生2硕士研究生3博士研究生
}
