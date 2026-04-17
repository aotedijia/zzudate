package org.example.zzudate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.zzudate.dto.UserBaseInfoDto;
import org.example.zzudate.dto.UserSoulInfoDto;
import org.example.zzudate.entity.MatchResult;
import org.example.zzudate.entity.User;
import org.example.zzudate.mapper.MatchResultMapper;
import org.example.zzudate.mapper.UserMapper;
import org.example.zzudate.Result;
import org.example.zzudate.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.zzudate.service.UserService;
import org.example.zzudate.vo.MatchResultVo;

import java.util.Objects;

@RestController
@RequestMapping("/match")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MatchController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MatchResultMapper matchResultMapper;

    @PostMapping("savebaseinfo")
    public Result saveBaseInfo(@RequestBody UserBaseInfoDto userBaseInfoDto) {
        userBaseInfoDto.setId(CurrentUser.getUserId());
        System.out.println("收到基础信息同步请求");
        int tem=userService.saveBaseInfo(userBaseInfoDto);
        if(tem>0){
            return Result.success("同步成功");
        }else{
            return Result.error("同步失败");
        }
    }
    @PostMapping("saveuserinfo")
    public Result saveUserInfo(@RequestBody UserSoulInfoDto userSoulInfoDto) {
        System.out.println("收到深度信息同步请求");
        int tem=userService.saveSoulInfo(userSoulInfoDto);
        if(tem>0){
            return Result.success("同步成功");
        }else{
            return Result.error("同步失败");
        }
    }
    @PostMapping("getmatchresult")
    public Result getMatchResult(@RequestParam String userId) {
        System.out.println("11111");
        if(!userId.equals(CurrentUser.getUserId())){
            return Result.error("请不要攻击");
        }
        LambdaQueryWrapper<MatchResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchResult::getUserIdA,userId)
                .or()
                .eq(MatchResult::getUserIdB, userId);
        MatchResult matchResult = matchResultMapper.selectOne(wrapper);
        if (matchResult == null) {
            return Result.success("很遗憾本周没有匹配到人");
        }
        MatchResultVo  matchResultVo = new MatchResultVo();
        matchResultVo.setUserId(userId);
        boolean isUserA=userId.equals(matchResult.getUserIdA());
        String otherUserId=isUserA ? matchResult.getUserIdB() : matchResult.getUserIdA();
        matchResultVo.setMatchId(otherUserId);
        matchResultVo.setMatchUserName(isUserA ? matchResult.getUserNameB() : matchResult.getUserNameA());

        matchResultVo.setScore(matchResult.getScore());
        matchResultVo.setDescription(matchResult.getDescription());
// 1. 判断坦白指标
        boolean iHaveNumber = isUserA ? (matchResult.getNumberA() != null) : (matchResult.getNumberB() != null);
        boolean iHaveNumber2 = isUserA ? (matchResult.getNumberB() != null) : (matchResult.getNumberA() != null);

        matchResultVo.setIHaveNumber(iHaveNumber);
        matchResultVo.setIHaveNumber2(iHaveNumber2);

// 2. 核心：三阶信息分发逻辑
        if (iHaveNumber && iHaveNumber2) {
            // 【阶段 A：共鸣达成】
            // 双方都坦白了，我能看到对方的号码
            String otherNumber = isUserA ? matchResult.getNumberB() : matchResult.getNumberA();
            matchResultVo.setNumber(otherNumber);
        }
        else if (iHaveNumber) {
            // 【阶段 B：单向展示】
            // 我坦白了但对方没点，我只能看到我自己的号码（作为已公示的反馈）
            String myNumber = isUserA ? matchResult.getNumberA() : matchResult.getNumberB();
            matchResultVo.setNumber(myNumber);
        }
        else {
            // 【阶段 C：信息封锁】
            // 只要我没点坦白，无论对方点没点，我拿到的 number 永远是 null
            matchResultVo.setNumber(null);
        }

        return Result.success(matchResultVo);
    }
    @PostMapping("/shownumber")
    public Result showNumber(String userId) {
        if(!userId.equals(CurrentUser.getUserId())){
            return Result.error("请不要攻击");
        }
        User user=userMapper.selectById(userId);
        if(user==null||user.getNumber()==null) {
            return Result.error("未找到有效的联系方式，请先完善基础资料");
        }
        String number=user.getNumber();
        LambdaQueryWrapper<MatchResult> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(MatchResult::getUserIdA,userId)
                .or()
                .eq(MatchResult::getUserIdB,userId);
        MatchResult matchResult = matchResultMapper.selectOne(wrapper);
        if(matchResult==null){
            return Result.error("本周暂无匹配结果，无法进行坦白");
        }
        if(userId.equals(matchResult.getUserIdA())){
            matchResult.setNumberA(number);
        }else{
            matchResult.setNumberB(number);
        }
        matchResultMapper.updateById(matchResult);
        return Result.success("联系方式坦白成功");
    }
}
