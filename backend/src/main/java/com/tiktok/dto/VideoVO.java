package com.tiktok.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoVO {

    private Long id;

    private Long userId;

    private String videoUrl;

    private String title;

    private Integer likeCount;

    private LocalDateTime createTime;
}
