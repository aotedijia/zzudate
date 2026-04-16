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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.zzudate.service.UserService;
import org.example.zzudate.vo.MatchResultVo;

import java.util.Objects;

@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MatchResultMapper matchResultMapper;

    @PostMapping("savebaseinfo")
    public Result saveBaseInfo(UserBaseInfoDto userBaseInfoDto) {
        System.out.println("收到基础信息同步请求");
        int tem=userService.saveBaseInfo(userBaseInfoDto);
        if(tem>0){
            return Result.success("同步成功");
        }else{
            return Result.error("同步失败");
        }
    }
    @PostMapping("saveuserinfo")
    public Result saveUserInfo(UserSoulInfoDto userSoulInfoDto) {
        System.out.println("收到深度信息同步请求");
        int tem=userService.saveSoulInfo(userSoulInfoDto);
        if(tem>0){
            return Result.success("同步成功");
        }else{
            return Result.error("同步失败");
        }
    }
    @PostMapping("getmatchresult")
    public Result getMatchResult(String userId) {
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
        //判断自己是否坦白
        boolean iHaveNumber=isUserA ? (matchResult.getNumberA() != null) : (matchResult.getNumberB() != null);
        //判断对方是否已坦白
        boolean iHaveNumber2=isUserA ? (matchResult.getNumberB() != null) : (matchResult.getNumberA() != null);

        matchResultVo.setIHaveNumber(iHaveNumber);
        matchResultVo.setIHaveNumber2(iHaveNumber2);

        //只有对方坦白了 才能在Vo中看到联系方式
        if (iHaveNumber2) {
            String otherContact = isUserA ? matchResult.getNumberB() : matchResult.getNumberA();
            matchResultVo.setNumber(otherContact);
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
