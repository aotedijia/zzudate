package vo;

import lombok.Data;

@Data
public class TokenVo {
    String accessToken;
    Long userId;
    String email;
}
