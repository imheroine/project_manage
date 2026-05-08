package com.example.manage2.controller;

import com.example.manage2.common.Result;
import com.example.manage2.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin // 解决跨域问题
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. Controller 层只做最基础的参数校验
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 2. 调用 Service 层处理核心上传逻辑，并获取返回的文件访问路径
            String fileUrl = fileService.uploadFile(file);
            return Result.success(fileUrl, "上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传保存失败：" + e.getMessage());
        }
    }
}
