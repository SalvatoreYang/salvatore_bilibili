package com.salvatore.bilibili.service;

import com.mysql.cj.util.StringUtils;
import com.salvatore.bilibili.dao.FileDao;
import com.salvatore.bilibili.domain.File;
import com.salvatore.bilibili.service.util.FastDFSUtil;
import com.salvatore.bilibili.service.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
public class FileService {
    @Autowired
    private FileDao fileDao;
    @Autowired
    private FastDFSUtil fastDFSUtil;

    public String uploadFileBySlices(MultipartFile slice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws IOException {
        File dbFileByMD5 = fileDao.getFileByMD5(fileMd5);
        if (dbFileByMD5 != null){
            return dbFileByMD5.getUrl();
        }

        String url = fastDFSUtil.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        if (!StringUtils.isNullOrEmpty(url)){
            dbFileByMD5 = new File();
            dbFileByMD5.setCreateTime(new Date());
            dbFileByMD5.setMd5(fileMd5);
            dbFileByMD5.setType(fastDFSUtil.getFileType(slice));
            dbFileByMD5.setUrl(url);
            fileDao.addFile(dbFileByMD5);
        }
        return url;
    }

    public String getFileMD5(MultipartFile file) throws IOException {
        return MD5Util.getFileMD5(file);
    }
}
