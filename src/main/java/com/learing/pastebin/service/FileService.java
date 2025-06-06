package com.learing.pastebin.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.learing.pastebin.cache.FileCache;
import com.learing.pastebin.cache.UserFolderMappingCache;
import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.dao.UserFolderMappingRepository;
import com.learing.pastebin.dto.response.FileResponse;
import com.learing.pastebin.dto.request.FileUploadRequest;
import com.learing.pastebin.dto.response.FileSaveResponse;
import com.learing.pastebin.model.File;
import com.learing.pastebin.model.Folder;
import com.learing.pastebin.model.UserFolderMapping;
import com.learing.pastebin.util.UtilService;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger                                logger = LoggerFactory.getLogger(FileService.class);
    private final        FileRepository                        fileRepository;
    private final        UtilService                           utilService;
    private final        UserDataService                       userDataService;
    private final        UserFolderMappingRepository           userFolderMappingRepository;
    private final        FolderRepository                      folderRepository;
    private final        LoadingCache<String, File>            fileCaffeineCache;
    private final        FileCache                             fileCache;
    private final        LoadingCache<UUID, UserFolderMapping> userFolderMappingLoadingCache;
    private final        UserFolderMappingCache                userFolderMappingCache;

    public FileService(FileRepository fileRepository, UtilService utilService, UserDataService userDataService,
            UserFolderMappingRepository userFolderMappingRepository, FolderRepository folderRepository, FileCache fileCache,
            UserFolderMappingCache userFolderMappingCache) {

        this.utilService = utilService;
        this.userDataService = userDataService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userFolderMappingRepository = userFolderMappingRepository;
        this.fileCache = fileCache;
        this.userFolderMappingCache = userFolderMappingCache;
        this.fileCaffeineCache = Caffeine.newBuilder().maximumSize(1000).build(key -> {
            RBucket<File> bucket = this.fileCache.get(key);
            if (bucket != null) {
                return bucket.get();
            }
            return null;
        });
        this.userFolderMappingLoadingCache = Caffeine.newBuilder().maximumSize(1000).build(key -> {
            RBucket<UserFolderMapping> bucket = this.userFolderMappingCache.get(key);
            if (bucket != null) {
                return bucket.get();
            }
            return null;
        });
    }

    public FileSaveResponse saveData(FileUploadRequest fileUploadRequest) {
        FileSaveResponse fileSaveResponse = new FileSaveResponse();
        fileSaveResponse.setStatus("failure");
        try {
            File file = utilService.mapPasteBin(fileUploadRequest);
            if (file != null) {
                if (file.getUserId() != null) {
                    Folder folder = saveFolderData(file, fileUploadRequest.getFolderName());
                    file.setFolder(folder);
                }
                fileRepository.save(file);
                fileSaveResponse.setStatus("success");
                fileSaveResponse.setUrl("http://localhost:8088/paste-bin/get-data/" + file.getKey());
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
            File file = fileCaffeineCache.get(key);
            if (file != null && utilService.validatePassword(password, file.getPassword())) {
                LocalDateTime expiredAt = file.getExpiredAt();
                file.setViews(file.getViews() + 1);
                if (expiredAt.isAfter(LocalDateTime.now())) {
                    fileResponse.setContent(file.getContent());
                    if (file.isOnceView()) {
                        fileRepository.delete(file);
                        fileCache.delete(key);
                        fileCaffeineCache.invalidate(key);
                    } else {
                        fileRepository.updateViews(file.getId(), file.getViews());
                    }
                    utilService.mapPasteBinMetaData(file, fileResponse);
                } else {
                    fileResponse.setStatus("Paste bin expired");
                }
            } else {
                fileResponse.setStatus("File Not Found Invalid password or key");
            }

        } catch (Exception e) {
            logger.error("Error while getting data {}", e.getMessage());
        }
        return fileResponse;
    }

    private Folder saveFolderData(File file, String folderName) {
        Folder folder = null;
        try {
            UUID userId = file.getUserId();
            if (userId != null) {
                file.setUserId(userId);
                if (folderName == null || folderName.isBlank()) {
                    folderName = "Default";
                }
                UserFolderMapping userFolderMapping = getUserFolderMapping(userId);
                List<Folder> folders = userFolderMapping.getFolders();
                folder = getFolder(folders, folderName);
                file.setFolder(folder);
                if (file.getFolder() == null) {
                    createFolder(folderName, userFolderMapping, file);
                }
            }
        } catch (Exception e) {
            logger.error("Error while saving folder data {}", e.getMessage());
        }
        return folder;
    }

    private UserFolderMapping getUserFolderMapping(UUID userId) {
        UserFolderMapping userFolderMapping = userFolderMappingRepository.getByUserId(userId);
        if (userFolderMapping == null) {
            userFolderMapping = new UserFolderMapping();
            userFolderMapping.setUserId(userId);
            userFolderMappingRepository.save(userFolderMapping);
        }
        return userFolderMapping;
    }

    private Folder getFolder(List<Folder> folders, String folderName) {
        for (Folder ifolder : folders) {
            if (ifolder.getFolderName().equals(folderName)) {
                return ifolder;
            }
        }
        return null;
    }

    private void createFolder(String folderName, UserFolderMapping userFolderMapping, File file) {
        Folder folder = new Folder();
        folder.setFolderName(folderName);
        folder.getFiles().add(file);
        folder.setUserFolderMapping(userFolderMapping);
        folderRepository.save(folder);
        userFolderMapping.getFolders().add(folder);
        userFolderMappingRepository.save(userFolderMapping);
    }
}
