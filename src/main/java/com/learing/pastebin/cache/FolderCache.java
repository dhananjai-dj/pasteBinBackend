package com.learing.pastebin.cache;

import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.dao.FolderRepository;
import com.learing.pastebin.model.Folder;
import com.learing.pastebin.model.Folder;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class FolderCache {
    private final RedissonClient redissonClient;

    private final FolderRepository folderRepository;

    public FolderCache(RedissonClient redissonClient, FolderRepository folderRepository) {
        this.redissonClient = redissonClient;
        this.folderRepository = folderRepository;
    }

    public RBucket<Folder> get(String key) {
        RBucket<Folder> bucket = redissonClient.getBucket(key);
        if (bucket != null) {
            Folder folder = bucket.get();
            if (folder != null) {
                return bucket;
            }
        }
        Folder folder = folderRepository.findByFolderId(Long.parseLong(key.replace("FOLDER_", "")));
        put(key, folder);
        return redissonClient.getBucket(key);
    }

    public void put(String bucketName, Folder folder) {
        redissonClient.getBucket(bucketName).set(folder);
    }

    public void delete(String bucketName) {
        redissonClient.getBucket(bucketName).delete();
    }
}
