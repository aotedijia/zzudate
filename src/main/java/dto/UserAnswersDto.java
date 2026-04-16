package dto;

import lombok.Data;

@Data
public class UserAnswersDto {
    private Long userId;
    private String answers;//前端传进来答案转化的JSON字符串
}
