package com.learing.pastebin.service;

import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.dao.UserFolderMappingRepository;
import com.learing.pastebin.dto.FolderResponse;
import com.learing.pastebin.dto.UserData;
import com.learing.pastebin.model.File;
import com.learing.pastebin.model.Folder;
import com.learing.pastebin.model.UserFolderMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserDataService {
    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);

    private final FolderRepository            folderRepository;
    private final UserFolderMappingRepository userFolderMappingRepository;
    private final FileRepository              fileRepository;

    public UserDataService(FolderRepository folderRepository, UserFolderMappingRepository userFolderMappingRepository,
            FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.userFolderMappingRepository = userFolderMappingRepository;
        this.fileRepository = fileRepository;
    }

    public boolean saveFolderData(File file) {
        try {
            if (file.getFolder().getFolderName() != null && file.getUserId() != null) {
                Folder folder = folderRepository.findByFolderId(file.getFolder().getFolderId());
                if (folder == null) {
                    folder = new Folder();
                }
                folder.setFolderName(file.getFolder().getFolderName());
                folder.getFiles().add(file);
                folderRepository.save(folder);
                return userFolderMapping(file.getUserId(), file.getFolder().getFolderId());
            }
        } catch (Exception e) {
            logger.error("Error while saving folder data {}", e.getMessage());
        }
        return false;
    }

    public UserData getUserData(UUID userId) {
        UserData userData = new UserData();
        try {
            UserFolderMapping userFolderMapping = userFolderMappingRepository.getByUserId(userId);
            userData.setName(userFolderMapping.getUserName());
            List<Folder> list = userFolderMapping.getFolders();
            if (list != null && !list.isEmpty()) {
                long totalFiles = 0L;
                long totalViews = 0L;
                long activePastes = 0L;
                long publicPastes = 0L;
                List<Map<Long, String>> folderNames = new ArrayList<>();

                for (Folder folder : list) {
                    folderNames.add(Map.of(folder.getFolderId(), folder.getFolderName()));
                    List<File> files = folder.getFiles();
                    totalFiles += files.size();

                    for (File file : files) {
                        totalViews += file.getViews();
                        if (!file.isPrivate())
                            publicPastes++;
                        if (LocalDateTime.now().isBefore(file.getExpiredAt()))
                            activePastes++;
                    }
                }

                userData.setTotalPastes(totalFiles);
                userData.setFolders(folderNames);
                userData.setTotalViews(totalViews);
                userData.setPublicPastes(publicPastes);
                userData.setActivePastes(activePastes);

            }
        } catch (Exception e) {
            logger.error("Error while getting user data {}", e.getMessage());
        }
        return userData;
    }

    public FolderResponse getFolderData(long folderId) {
        FolderResponse folderResponse = new FolderResponse();
        try {
            Folder folder = folderRepository.findByFolderId(folderId);
            if (folder != null) {
                folderResponse.setName(folder.getFolderName());
                folderResponse.setFolderId(folderId);
                folderResponse.setCreatedAt(LocalDateTime.now());
                folderResponse.setUpdatedAt(LocalDateTime.now());
                List<File> files = folder.getFiles();
                if (files != null && !files.isEmpty()) {
                    List<String> folderNames = files.stream().map(File::getKey).toList();
                    if (!folderNames.isEmpty()) {
                        folderResponse.setFileLinks(folderNames);
                    } else {
                        folderResponse.setFileLinks(List.of("No files found"));
                    }
                    folderResponse.setStatus("success");
                }
            }
        } catch (Exception e) {
            logger.error("Error while getting folder {}", e.getMessage());
        }
        return folderResponse;
    }

    public boolean userFolderMapping(UUID userId, long folderId) {
        UserFolderMapping userFolderMapping = userFolderMappingRepository.getByUserId(userId);
        if (userFolderMapping == null) {
            userFolderMapping = new UserFolderMapping();
        }
        userFolderMapping.setFolderId(folderId);
        userFolderMapping.setUserId(userId);
        userFolderMappingRepository.save(userFolderMapping);
        return true;
    }

    public String changeFolderData(long pasteBinId, long newFolderId) {
        String status = "failure";
        File file = fileRepository.findById(pasteBinId).orElse(null);
        if (file != null) {
            Folder existingFolder = folderRepository.findByFolderId(file.getFolder().getFolderId());
            if (existingFolder != null) {
                existingFolder.getFiles().remove(file);
                Folder newFolder = folderRepository.findByFolderId(newFolderId);
                if (newFolder != null) {
                    newFolder.getFiles().add(file);
                    file.getFolder().setFolderName(newFolder.getFolderName());
                    file.getFolder().setFolderId(newFolderId);
                    fileRepository.save(file);
                    folderRepository.save(newFolder);
                    folderRepository.save(existingFolder);
                    status = "success";
                }
            }
        }
        return status;
    }

    public FolderResponse updateFolderName(long folderId, String newName) {
        try {
            Folder folder = folderRepository.findByFolderId(folderId);
            if (folder != null) {
                folder.setFolderName(newName);
                folderRepository.save(folder);
                return getFolderData(folderId);
            }
        } catch (Exception e) {
            logger.error("Error while updating folder name {}", e.getMessage());
        }
        FolderResponse folderResponse = new FolderResponse();
        folderResponse.setFolderId(folderId);
        folderResponse.setStatus("Failed to update Folder Name");
        return folderResponse;
    }

}
