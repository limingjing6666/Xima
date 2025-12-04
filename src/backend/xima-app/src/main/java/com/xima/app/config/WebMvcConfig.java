package com.xima.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Web MVC 配置
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:C:/Users/23725/Desktop/Xima/uploads/avatars}")
    private String avatarDir;

    @Value("${app.upload.background-dir:C:/Users/23725/Desktop/Xima/uploads/backgrounds}")
    private String backgroundDir;

    @Value("${file.upload-dir:C:/Users/23725/Desktop/Xima/uploads/}")
    private String baseUploadDir;

    @PostConstruct
    public void init() {
        // 确保目录存在
        createDirIfNotExists(avatarDir);
        createDirIfNotExists(backgroundDir);
        createDirIfNotExists(baseUploadDir + "chat/images");
        createDirIfNotExists(baseUploadDir + "chat/files");
        
        log.info("头像上传目录: {}", avatarDir);
        log.info("背景上传目录: {}", backgroundDir);
        log.info("基础上传目录: {}", baseUploadDir);
    }

    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建目录: {}", dir.getAbsolutePath());
        }
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置头像静态资源访问
        String avatarLocation = "file:///" + avatarDir.replace("\\", "/") + "/";
        log.info("静态资源映射: /uploads/avatars/** -> {}", avatarLocation);
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarLocation);
        
        // 配置背景图片静态资源访问
        String bgLocation = "file:///" + backgroundDir.replace("\\", "/") + "/";
        log.info("静态资源映射: /uploads/backgrounds/** -> {}", bgLocation);
        registry.addResourceHandler("/uploads/backgrounds/**")
                .addResourceLocations(bgLocation);
        
        // 配置聊天图片静态资源访问
        String chatImgPath = baseUploadDir + "chat/images";
        String chatImgLocation = "file:///" + chatImgPath.replace("\\", "/") + "/";
        log.info("静态资源映射: /uploads/chat/images/** -> {}", chatImgLocation);
        registry.addResourceHandler("/uploads/chat/images/**")
                .addResourceLocations(chatImgLocation);
        
        // 配置聊天文件静态资源访问
        String chatFilePath = baseUploadDir + "chat/files";
        String chatFileLocation = "file:///" + chatFilePath.replace("\\", "/") + "/";
        log.info("静态资源映射: /uploads/chat/files/** -> {}", chatFileLocation);
        registry.addResourceHandler("/uploads/chat/files/**")
                .addResourceLocations(chatFileLocation);
    }
}
