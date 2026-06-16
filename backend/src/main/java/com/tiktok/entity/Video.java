package com.tiktok.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video")
public class Video {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String videoUrl;

    private String title;

    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
