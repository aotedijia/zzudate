package org.example.zzudate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.zzudate.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
