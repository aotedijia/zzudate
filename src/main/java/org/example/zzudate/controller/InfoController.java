package org.example.zzudate.controller;

import org.example.zzudate.Result;
import org.example.zzudate.entity.Info;
import org.example.zzudate.service.InfoService;
import org.example.zzudate.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("Info")
public class InfoController {
    @Autowired
    private InfoService infoService;

    @PostMapping("saveInfo")
    public Result<Object> saveInfo(@RequestBody Info info){
        // 前端传入 userId 和 userName，后端用当前登录用户校验
        String currentUserId = CurrentUser.getUserId();
        if(info.getUserId() == null){
            info.setUserId(currentUserId);
        }
        if(infoService.saveInfo(info)>0){
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    @PostMapping("updateInfo")
    public Result updateInfo(@RequestBody Info info){
        if(info.getUserId()!= CurrentUser.getUserId()){
            return Result.error("请不要攻击");
        }
        if(infoService.updateInfo(info)>0){
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PostMapping("deleteInfo")
    public Result deleteInfo(@RequestBody Info info){
        if(info.getUserId()!= CurrentUser.getUserId()){
            return Result.error("请不要攻击");
        }
        if(infoService.deleteInfo(info.getId())>0){
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("list")
    public Result<List<Info>> listAll(){
        return Result.success(infoService.listAll());
    }

    @GetMapping("listByCategory")
    public Result<List<Info>> listByCategory(@RequestParam String category){
        return Result.success(infoService.listByCategory(category));
    }
}
