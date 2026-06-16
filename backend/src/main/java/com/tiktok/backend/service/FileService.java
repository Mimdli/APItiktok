package com.tiktok.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    @Value("${tiktok.upload.path}")
    private String uploadPath;

    public String saveVideo(MultipartFile file) throws IOException {
        File folder = new File(uploadPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        File targetFile = new File(uploadPath + filename);
        file.transferTo(targetFile);

        return filename; // 返回文件名或相对路径
    }
}
