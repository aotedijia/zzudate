package org.example.zzudate.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Info {
    private String id;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String category;//分区 公共讨论区 校医院 餐厅 学习备考
}
