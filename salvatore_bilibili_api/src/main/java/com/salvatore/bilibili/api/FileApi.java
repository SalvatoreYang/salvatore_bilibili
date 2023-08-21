package com.salvatore.bilibili.api;

import com.salvatore.bilibili.domain.JsonResponse;
import com.salvatore.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileApi {
    
    @Autowired
    private FileService fileService;

    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws IOException {
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }

    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws IOException {
        String filePath = fileService.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);
    }
}