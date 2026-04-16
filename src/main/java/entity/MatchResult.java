package entity;

import lombok.Data;

import java.util.Map;

@Data
public class MatchResult {
    String id;
    String userIdA;
    String userIdB;
    String userNameA;
    String userNameB;
    String userAnswerA;
    String userAnswerB;
    Double score;
    String description;
    String numberA;//A联系方式
    String numberB;//B联系方式
}
