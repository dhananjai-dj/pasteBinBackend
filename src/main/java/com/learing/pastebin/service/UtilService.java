package com.learing.pastebin.service;

import com.learing.pastebin.dto.FileResponse;
import com.learing.pastebin.dto.FileRequest;
import com.learing.pastebin.model.File;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UtilService {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public File mapPasteBin(FileRequest fileRequest) {
        File file = new File();
        file.setKey(generateKey());
        file.setSize(fileRequest.getSize());
        file.setType(fileRequest.getType());
        file.setTitle(fileRequest.getTitle());
        file.setPrivate(fileRequest.isPrivate());
        file.setContent(fileRequest.getContent());
        file.setOnceView(fileRequest.isOnceView());
        file.setLanguage(fileRequest.getLanguage());
        file.setPassword(hashPassword(fileRequest.getPassword()));
        file.setExpiredAt(LocalDateTime.now().plusMinutes(fileRequest.getDuration()));
        if (fileRequest.getUserId() != null) {
            long folderId = fileRequest.getFolderId();
            String folderName = fileRequest.getFolderName();
            if (folderName == null || folderName.isEmpty()) {
                folderName = "Default";
            }
            file.setUserId(fileRequest.getUserId());
            file.getFolder().setFolderId(folderId);
            file.getFolder().setFolderName(folderName);
        }
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
