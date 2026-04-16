package vo;

import lombok.Data;

@Data
public class MatchResultVo {
    String userId;
    String matchId;
    String userName;
    String matchUserName;
    String description;
    Double score;
    String number;
    Boolean iHaveNumber;
    Boolean iHaveNumber2;
}
