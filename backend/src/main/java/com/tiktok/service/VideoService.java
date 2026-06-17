package com.tiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiktok.common.BusinessException;
import com.tiktok.dto.VideoVO;
import com.tiktok.entity.Video;
import com.tiktok.entity.VideoLike;
import com.tiktok.mapper.VideoLikeMapper;
import com.tiktok.mapper.VideoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoLikeMapper videoLikeMapper;

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

        return toVO(video, userId);
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

        List<VideoVO> records = toVOList(videoPage.getRecords(), userId);

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

    // ==================== 点赞系统 ====================

    @Transactional
    public void likeVideo(Long userId, Long videoId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        Long count = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getUserId, userId)
                .eq(VideoLike::getVideoId, videoId));
        if (count > 0) {
            return;
        }

        VideoLike like = new VideoLike();
        like.setUserId(userId);
        like.setVideoId(videoId);
        videoLikeMapper.insert(like);

        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, videoId)
                .setSql("like_count = like_count + 1"));
    }

    @Transactional
    public void unlikeVideo(Long userId, Long videoId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        int deleted = videoLikeMapper.delete(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getUserId, userId)
                .eq(VideoLike::getVideoId, videoId));
        if (deleted == 0) {
            return;
        }

        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, videoId)
                .gt(Video::getLikeCount, 0)
                .setSql("like_count = like_count - 1"));
    }

    // ==================== 推荐视频流 ====================

    public List<VideoVO> getFeed(Long userId, List<Long> excludeIds, int size) {
        if (size < 1 || size > 50) {
            size = 10;
        }

        // 收集需要排除的视频 ID：已浏览的 + 已点赞的
        Set<Long> excluded = new HashSet<>();
        if (excludeIds != null) {
            excluded.addAll(excludeIds);
        }
        if (userId != null) {
            List<Long> likedIds = videoLikeMapper.selectList(
                            new LambdaQueryWrapper<VideoLike>()
                                    .eq(VideoLike::getUserId, userId))
                    .stream()
                    .map(VideoLike::getVideoId)
                    .collect(Collectors.toList());
            excluded.addAll(likedIds);
        }

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .orderByDesc(Video::getLikeCount)
                .orderByDesc(Video::getId);

        if (!excluded.isEmpty()) {
            wrapper.notIn(Video::getId, excluded);
        }

        Page<Video> videoPage = videoMapper.selectPage(new Page<>(1, size), wrapper);
        return toVOList(videoPage.getRecords(), userId);
    }

    public VideoVO getNextVideo(Long currentId, Long userId, List<Long> excludeIds) {
        Video current = videoMapper.selectById(currentId);
        if (current == null) {
            throw new BusinessException("当前视频不存在");
        }

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .and(w -> w
                        .lt(Video::getLikeCount, current.getLikeCount())
                        .or()
                        .eq(Video::getLikeCount, current.getLikeCount())
                        .lt(Video::getId, currentId))
                .orderByDesc(Video::getLikeCount)
                .orderByDesc(Video::getId);

        if (excludeIds != null && !excludeIds.isEmpty()) {
            excludeIds.forEach(id -> {
                if (!id.equals(currentId)) {
                    wrapper.ne(Video::getId, id);
                }
            });
        }

        Page<Video> page = videoMapper.selectPage(new Page<>(1, 1), wrapper);
        if (page.getRecords().isEmpty()) {
            return null;
        }
        return toVO(page.getRecords().get(0), userId);
    }

    public VideoVO getPrevVideo(Long currentId, Long userId, List<Long> excludeIds) {
        Video current = videoMapper.selectById(currentId);
        if (current == null) {
            throw new BusinessException("当前视频不存在");
        }

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .and(w -> w
                        .gt(Video::getLikeCount, current.getLikeCount())
                        .or()
                        .eq(Video::getLikeCount, current.getLikeCount())
                        .gt(Video::getId, currentId))
                .orderByAsc(Video::getLikeCount)
                .orderByAsc(Video::getId);

        if (excludeIds != null && !excludeIds.isEmpty()) {
            excludeIds.forEach(id -> {
                if (!id.equals(currentId)) {
                    wrapper.ne(Video::getId, id);
                }
            });
        }

        Page<Video> page = videoMapper.selectPage(new Page<>(1, 1), wrapper);
        if (page.getRecords().isEmpty()) {
            return null;
        }
        return toVO(page.getRecords().get(0), userId);
    }

    // ==================== VO 转换 ====================

    private List<VideoVO> toVOList(List<Video> videos, Long userId) {
        if (videos.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> likedVideoIds = Collections.emptySet();
        if (userId != null) {
            List<Long> videoIds = videos.stream().map(Video::getId).collect(Collectors.toList());
            likedVideoIds = videoLikeMapper.selectList(new LambdaQueryWrapper<VideoLike>()
                            .eq(VideoLike::getUserId, userId)
                            .in(VideoLike::getVideoId, videoIds))
                    .stream()
                    .map(VideoLike::getVideoId)
                    .collect(Collectors.toSet());
        }

        Set<Long> finalLikedVideoIds = likedVideoIds;
        return videos.stream()
                .map(v -> {
                    VideoVO vo = toVO(v, userId);
                    vo.setLiked(finalLikedVideoIds.contains(v.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private VideoVO toVO(Video video, Long userId) {
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        vo.setVideoUrl(fileService.toAccessUrl(video.getVideoUrl()));
        if (userId != null) {
            Long count = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                    .eq(VideoLike::getUserId, userId)
                    .eq(VideoLike::getVideoId, video.getId()));
            vo.setLiked(count > 0);
        }
        return vo;
    }
}
