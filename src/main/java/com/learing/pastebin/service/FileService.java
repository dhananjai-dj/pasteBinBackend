package com.learing.pastebin.service;

import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.dto.FileResponse;
import com.learing.pastebin.dto.FileRequest;
import com.learing.pastebin.dto.FileSaveResponse;
import com.learing.pastebin.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FileService {

    private static final Logger          logger = LoggerFactory.getLogger(FileService.class);
    private final        FileRepository  fileRepository;
    private final        UtilService     utilService;
    private final        UserDataService userDataService;

    public FileService(FileRepository fileRepository, UtilService utilService, UserDataService userDataService) {
        this.fileRepository = fileRepository;
        this.utilService = utilService;
        this.userDataService = userDataService;
    }

    public FileSaveResponse saveData(FileRequest fileRequest) {
        FileSaveResponse fileSaveResponse = new FileSaveResponse();
        fileSaveResponse.setStatus("failure");
        try {
            File file = utilService.mapPasteBin(fileRequest);
            if (file != null) {
                fileRepository.save(file);
                fileSaveResponse.setStatus("success");
                fileSaveResponse.setUrl("http://localhost:8080/paste-bin/get-data/" + file.getKey());
                if (file.getUserId() != null) {
                    boolean isSavedInUserFolder = userDataService.saveFolderData(file);
                    if (isSavedInUserFolder) {
                        fileSaveResponse.setStatus("successfully saved in user folder!!!");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while saving data", e);
        }
        return fileSaveResponse;
    }

    public FileResponse getData(String key, String password) {
        FileResponse fileResponse = new FileResponse();
        try {
            File file = fileRepository.getByKey(key);
            if (file != null && utilService.validatePassword(password, file.getPassword())) {
                LocalDateTime expiredAt = file.getExpiredAt();
                file.setViews(file.getViews() + 1);
                if (expiredAt.isAfter(LocalDateTime.now())) {
                    fileResponse.setContent(file.getContent());
                    if (file.isOnceView()) {
                        fileRepository.delete(file);
                    } else {
                        fileRepository.save(file);
                    }
                    utilService.mapPasteBinMetaData(file, fileResponse);
                } else {
                    fileResponse.setStatus("Paste bin expired");
                }
            } else {
                fileResponse.setStatus("Invalid password");
            }

        } catch (Exception e) {
            logger.error("Error while getting data {}", e.getMessage());
        }
        return fileResponse;
    }
}
