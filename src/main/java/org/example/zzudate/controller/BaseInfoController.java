package org.example.zzudate.controller;

import org.example.zzudate.Result;
import org.example.zzudate.entity.Info;
import org.example.zzudate.service.InfoService;
import org.example.zzudate.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("BaseInfo")
public class BaseInfoController {
    @Autowired
    private InfoService infoService;
    @PostMapping("savaInfo")
    public Result savaInfo(@RequestBody Info info){
        if(infoService.saveInfo(info)!=0){
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }
    @PostMapping("updateInfo")
    public Result updateInfo(@RequestBody Info info){
        if(info.getUserId()!= CurrentUser.getUserId()){
            return Result.error("请不要攻击");
        }
        if(infoService.updateInfo(info)!=0){
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PostMapping("deleteInfo")
    public Result deleteInfo(@RequestBody Info info){
        if(info.getUserId()!=CurrentUser.getUserId()){
            return Result.error("请不要攻击");
        }
        if(infoService.deleteInfo(info.getId())!=0){
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
