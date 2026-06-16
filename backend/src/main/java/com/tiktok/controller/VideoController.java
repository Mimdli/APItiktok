package com.tiktok.controller;

import com.tiktok.common.Result;
import com.tiktok.dto.VideoVO;
import com.tiktok.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/publish")
    public Result<VideoVO> publish(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) throws Exception {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(videoService.publish(userId, file, title));
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> myVideos(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(videoService.getMyVideos(userId, page, size));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteVideo(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        videoService.deleteVideo(userId, id);
        return Result.success();
    }

    // ==================== 点赞系统 ====================

    @PostMapping("/like/{videoId}")
    public Result<Void> likeVideo(
            HttpServletRequest request,
            @PathVariable Long videoId) {
        Long userId = (Long) request.getAttribute("userId");
        videoService.likeVideo(userId, videoId);
        return Result.success();
    }

    @PostMapping("/unlike/{videoId}")
    public Result<Void> unlikeVideo(
            HttpServletRequest request,
            @PathVariable Long videoId) {
        Long userId = (Long) request.getAttribute("userId");
        videoService.unlikeVideo(userId, videoId);
        return Result.success();
    }

    // ==================== 推荐视频流 ====================

    @GetMapping("/feed")
    public Result<List<VideoVO>> feed(
            HttpServletRequest request,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(videoService.getFeed(userId, excludeIds, size));
    }

    @GetMapping("/next/{currentId}")
    public Result<VideoVO> nextVideo(
            HttpServletRequest request,
            @PathVariable Long currentId,
            @RequestParam(required = false) List<Long> excludeIds) {
        VideoVO vo = videoService.getNextVideo(currentId, excludeIds);
        return Result.success(vo);
    }

    @GetMapping("/prev/{currentId}")
    public Result<VideoVO> prevVideo(
            HttpServletRequest request,
            @PathVariable Long currentId,
            @RequestParam(required = false) List<Long> excludeIds) {
        VideoVO vo = videoService.getPrevVideo(currentId, excludeIds);
        return Result.success(vo);
    }
}
