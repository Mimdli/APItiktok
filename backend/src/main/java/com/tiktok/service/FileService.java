package com.tiktok.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".mp4", ".mov", ".avi", ".mkv", ".webm", ".flv"
    );

    @Value("${tiktok.upload.path}")
    private String uploadPath;

    public String saveVideo(MultipartFile file) throws IOException {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件格式不支持");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的视频格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 3. 保存文件
        File folder = new File(uploadPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filename = UUID.randomUUID().toString() + extension;
        File targetFile = new File(uploadPath + filename);
        file.transferTo(targetFile);

        return filename;
    }

    public void deleteVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.contains("/") || videoUrl.contains("\\")) {
            return;
        }
        File file = new File(uploadPath + videoUrl);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public String toAccessUrl(String videoUrl) {
        if (videoUrl == null) {
            return null;
        }
        if (videoUrl.contains("/") || videoUrl.contains("\\")) {
            return videoUrl;
        }
        return "/uploads/" + videoUrl;
    }
}
