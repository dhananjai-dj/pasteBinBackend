package com.learing.pastebin.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.dao.UserFolderMappingRepository;
import com.learing.pastebin.dto.response.FileResponse;
import com.learing.pastebin.dto.request.FileUploadRequest;
import com.learing.pastebin.dto.response.FileSaveResponse;
import com.learing.pastebin.model.File;
import com.learing.pastebin.model.Folder;
import com.learing.pastebin.model.UserFolderMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger                      logger = LoggerFactory.getLogger(FileService.class);
    private final        FileRepository              fileRepository;
    private final        UtilService                 utilService;
    private final        UserDataService             userDataService;
    private final        UserFolderMappingRepository userFolderMappingRepository;
    private final        FolderRepository            folderRepository;
    private final        LoadingCache<String, File>  fileCache;

    public FileService(FileRepository fileRepository, UtilService utilService, UserDataService userDataService,
            UserFolderMappingRepository userFolderMappingRepository, FolderRepository folderRepository) {

        this.utilService = utilService;
        this.userDataService = userDataService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userFolderMappingRepository = userFolderMappingRepository;
        this.fileCache = Caffeine.newBuilder().maximumSize(1000).build(fileRepository::getByKey);
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
            File file = fileCache.get(key);
            if (file != null && utilService.validatePassword(password, file.getPassword())) {
                LocalDateTime expiredAt = file.getExpiredAt();
                file.setViews(file.getViews() + 1);
                if (expiredAt.isAfter(LocalDateTime.now())) {
                    fileResponse.setContent(file.getContent());
                    if (file.isOnceView()) {
                        fileRepository.delete(file);
                        fileCache.invalidate(key);
                    } else {
                        fileRepository.save(file);
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

    public Folder saveFolderData(File file, String folderName) {
        Folder folder = null;
        try {
            UUID userId = file.getUserId();
            if (userId != null) {
                file.setUserId(userId);
                if (folderName != null && folderName.isEmpty()) {
                    folderName = "Default";
                }
                UserFolderMapping userFolderMapping = userFolderMappingRepository.getByUserId(userId);
                if (userFolderMapping == null) {
                    userFolderMapping = new UserFolderMapping();
                    userFolderMapping.setUserId(userId);
                    userFolderMappingRepository.save(userFolderMapping);
                }
                List<Folder> folders = userFolderMapping.getFolders();
                for (Folder ifolder : folders) {
                    if (ifolder.getFolderName().equals(folderName)) {
                        file.setFolder(folder);
                        folder = ifolder;
                        break;
                    }
                }
                if (file.getFolder() == null) {
                    folder = new Folder();
                    folder.setFolderName(folderName);
                    folder.getFiles().add(file);
                    folder.setUserFolderMapping(userFolderMapping);
                    folderRepository.save(folder);
                    folders.add(folder);
                    userFolderMapping.setFolders(folders);
                    userFolderMappingRepository.save(userFolderMapping);
                }
            }
        } catch (Exception e) {
            logger.error("Error while saving folder data {}", e.getMessage());
        }
        return folder;
    }
}
