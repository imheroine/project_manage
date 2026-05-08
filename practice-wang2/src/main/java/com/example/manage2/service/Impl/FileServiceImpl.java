package com.example.manage2.service.Impl;

import com.example.manage2.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    // 获取当前项目的根目录，并指定一个 uploads 文件夹存放附件
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 如果目录不存在，自动创建目录
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. 获取文件的原始名称和后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. 使用 UUID 生成新文件名，防止文件被覆盖
            String newFilename = UUID.randomUUID().toString() + suffix;

            // 4. 将文件保存到硬盘
            File destFile = new File(UPLOAD_DIR + newFilename);
            file.transferTo(destFile);

            // 5. 生成文件的相对访问路径
            String fileUrl = "/uploads/" + newFilename;

            // 6. 返回路径供 Controller 使用
            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("文件读写异常", e);
        }
    }
}
