package org.example.zzudate.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.zzudate.entity.Info;
import org.example.zzudate.mapper.InfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfoServiceImpl implements InfoService {
    @Autowired
    private InfoMapper infoMapper;

    public int saveInfo(Info info) {
        return infoMapper.insert(info);
    }

    public int updateInfo(Info info) {
        return infoMapper.updateById(info);
    }

    public int deleteInfo(String id) {
        return infoMapper.deleteById(id);
    }

    @Override
    public List<Info> listAll() {
        QueryWrapper<Info> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return infoMapper.selectList(wrapper);
    }

    @Override
    public List<Info> listByCategory(String category) {
        QueryWrapper<Info> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category).orderByDesc("create_time");
        return infoMapper.selectList(wrapper);
    }
}
