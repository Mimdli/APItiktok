package com.tiktok.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiktok.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
