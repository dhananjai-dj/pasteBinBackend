package com.learing.pastebin.util;

import com.learing.pastebin.dto.response.FileResponse;
import com.learing.pastebin.dto.request.FileUploadRequest;
import com.learing.pastebin.model.File;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UtilService {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public File mapPasteBin(FileUploadRequest fileUploadRequest) {
        File file = new File();
        file.setKey(generateKey());
        file.setSize(fileUploadRequest.getSize());
        file.setType(fileUploadRequest.getType());
        file.setTitle(fileUploadRequest.getTitle());
        file.setUserId(fileUploadRequest.getUserId());
        file.setPrivate(fileUploadRequest.isPrivate());
        file.setContent(fileUploadRequest.getContent());
        file.setOnceView(fileUploadRequest.isOnceView());
        file.setLanguage(fileUploadRequest.getLanguage());
        file.setPassword(hashPassword(fileUploadRequest.getPassword()));
        file.setExpiredAt(LocalDateTime.now().plusMinutes(fileUploadRequest.getDuration()));
        return file;
    }

    public void mapPasteBinMetaData(File file, FileResponse fileResponse) {
        FileResponse.MetaData metaData = new FileResponse.MetaData();
        metaData.setViews(file.getViews());
        metaData.setType(file.getType());
        metaData.setTitle(file.getTitle());
        metaData.setLanguage(file.getLanguage());
        metaData.setPrivate(file.isPrivate());
        metaData.setOnceView(file.isOnceView());
        fileResponse.setMetaData(metaData);
        fileResponse.setStatus("success");
    }

    private static String generateKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean validatePassword(String password, String hashPassword) {
        return passwordEncoder.matches(password, hashPassword);
    }
}
