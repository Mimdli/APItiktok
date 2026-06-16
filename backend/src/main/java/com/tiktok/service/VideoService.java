package com.tiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiktok.common.BusinessException;
import com.tiktok.dto.VideoVO;
import com.tiktok.entity.Video;
import com.tiktok.mapper.VideoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private FileService fileService;

    public VideoVO publish(Long userId, MultipartFile file, String title) throws IOException {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("视频标题不能为空");
        }
        if (title.length() > 128) {
            throw new BusinessException("视频标题长度不能超过128个字符");
        }

        String filename = fileService.saveVideo(file);

        Video video = new Video();
        video.setUserId(userId);
        video.setVideoUrl(filename);
        video.setTitle(title);
        video.setLikeCount(0);
        videoMapper.insert(video);

        return toVO(video);
    }

    public Map<String, Object> getMyVideos(Long userId, int page, int size) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 10;
        }

        Page<Video> videoPage = videoMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getUserId, userId)
                        .orderByDesc(Video::getCreateTime)
        );

        List<VideoVO> records = videoPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", videoPage.getTotal());
        result.put("page", videoPage.getCurrent());
        result.put("size", videoPage.getSize());
        result.put("pages", videoPage.getPages());
        return result;
    }

    public void deleteVideo(Long userId, Long videoId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (!video.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该视频");
        }

        videoMapper.deleteById(videoId);
        fileService.deleteVideo(video.getVideoUrl());
    }

    private VideoVO toVO(Video video) {
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        vo.setVideoUrl(fileService.toAccessUrl(video.getVideoUrl()));
        return vo;
    }
}
