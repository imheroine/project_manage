package com.example.manage2.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 处理文件上传逻辑
     *
     * @param file 前端上传的文件流
     * @return 文件的相对访问路径 (例如: /uploads/xxx.pdf)
     */
    String uploadFile(MultipartFile file);
}