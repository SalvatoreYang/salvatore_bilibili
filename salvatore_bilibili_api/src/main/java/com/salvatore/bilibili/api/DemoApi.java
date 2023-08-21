package com.salvatore.bilibili.api;


import com.salvatore.bilibili.service.DemoService;

import com.salvatore.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class DemoApi {

    @Autowired
    private DemoService demoService;
    @Autowired
    private FastDFSUtil fastDFSUtil;

    @GetMapping("/query")
    public Long query(Long id){
        return demoService.query(id);
    }

    @GetMapping("/slices")
    public void slices(MultipartFile file) throws IOException {
        fastDFSUtil.convertFileToSlices(file);
    }
}
