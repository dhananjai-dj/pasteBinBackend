package com.learing.pastebin.service;

import com.learing.pastebin.dto.PasteBinDataResponse;
import com.learing.pastebin.dto.PasteBinRequest;
import com.learing.pastebin.model.PasteBin;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UtilService {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PasteBin mapPasteBin(PasteBinRequest pasteBinRequest) {
        PasteBin pasteBin = new PasteBin();
        pasteBin.setKey(generateKey());
        pasteBin.setSize(pasteBinRequest.getSize());
        pasteBin.setType(pasteBinRequest.getType());
        pasteBin.setTitle(pasteBinRequest.getTitle());
        pasteBin.setPrivate(pasteBinRequest.isPrivate());
        pasteBin.setContent(pasteBinRequest.getContent());
        pasteBin.setOnceView(pasteBinRequest.isOnceView());
        pasteBin.setLanguage(pasteBinRequest.getLanguage());
        pasteBin.setPassword(hashPassword(pasteBinRequest.getPassword()));
        pasteBin.setExpiredAt(LocalDateTime.now().plusMinutes(pasteBinRequest.getDuration()));
        return pasteBin;
    }

    public void mapPasteBinMetaData(PasteBin pasteBin, PasteBinDataResponse pasteBinDataResponse) {
        PasteBinDataResponse.MetaData metaData = new PasteBinDataResponse.MetaData();
        metaData.setViews(pasteBin.getViews());
        metaData.setType(pasteBin.getType());
        metaData.setTitle(pasteBin.getTitle());
        metaData.setLanguage(pasteBin.getLanguage());
        metaData.setPrivate(pasteBin.isPrivate());
        metaData.setOnceView(pasteBin.isOnceView());
        pasteBinDataResponse.setMetaData(metaData);
        pasteBinDataResponse.setStatus("success");
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
