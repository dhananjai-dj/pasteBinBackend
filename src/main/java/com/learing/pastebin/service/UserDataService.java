package com.learing.pastebin.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.learing.pastebin.cache.FolderCache;
import com.learing.pastebin.cache.UserFolderMappingCache;
import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.dao.UserFolderMappingRepository;
import com.learing.pastebin.dto.response.FolderResponse;
import com.learing.pastebin.dto.response.UserDataResponse;
import com.learing.pastebin.model.File;
import com.learing.pastebin.model.Folder;
import com.learing.pastebin.model.UserFolderMapping;
import org.redisson.api.RBucket;
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

    private final FolderRepository                      folderRepository;
    private final UserFolderMappingRepository           userFolderMappingRepository;
    private final FileRepository                        fileRepository;
    private final LoadingCache<UUID, UserFolderMapping> userFolderMappingLoadingCache;
    private final UserFolderMappingCache                userFolderMappingCache;
    private final FolderCache                           folderCache;
    private final LoadingCache<String, Folder>          folderLoadingCache;

    public UserDataService(FolderRepository folderRepository, UserFolderMappingRepository userFolderMappingRepository, FileRepository fileRepository,
            UserFolderMappingCache userFolderMappingCache, FolderCache folderCache) {
        this.folderRepository = folderRepository;
        this.userFolderMappingRepository = userFolderMappingRepository;
        this.fileRepository = fileRepository;
        this.userFolderMappingCache = userFolderMappingCache;
        this.folderCache = folderCache;
        this.folderLoadingCache = Caffeine.newBuilder().maximumSize(1000).build(key -> {
            RBucket<Folder> bucket = this.folderCache.get(key);
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

    public UserDataResponse getUserData(UUID userId) {
        UserDataResponse userDataResponse = new UserDataResponse();
        try {
            UserFolderMapping userFolderMapping = userFolderMappingLoadingCache.get(userId);
            userDataResponse.setName(userFolderMapping.getUserName());
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

                userDataResponse.setTotalPastes(totalFiles);
                userDataResponse.setFolders(folderNames);
                userDataResponse.setTotalViews(totalViews);
                userDataResponse.setPublicPastes(publicPastes);
                userDataResponse.setActivePastes(activePastes);

            }
        } catch (Exception e) {
            logger.error("Error while getting user data {}", e.getMessage());
        }
        return userDataResponse;
    }

    public FolderResponse getFolderData(long folderId) {
        FolderResponse folderResponse = new FolderResponse();
        try {
            Folder folder = folderLoadingCache.get("FOLDER_" + folderId);
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
        UserFolderMapping userFolderMapping = userFolderMappingLoadingCache.get(userId);
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
            Folder existingFolder = folderLoadingCache.get("FOLDER_" + file.getFolder().getFolderId());
            if (existingFolder != null) {
                existingFolder.getFiles().remove(file);
                Folder newFolder = folderLoadingCache.get("FOLDER_" + newFolderId);
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
            Folder folder = folderLoadingCache.get("FOLDER_" + folderId);
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
