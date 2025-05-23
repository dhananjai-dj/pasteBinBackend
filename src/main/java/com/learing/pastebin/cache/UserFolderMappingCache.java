package com.learing.pastebin.cache;

import com.learing.pastebin.dao.UserFolderMappingRepository;
import com.learing.pastebin.model.UserFolderMapping;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserFolderMappingCache {
    private final RedissonClient redissonClient;

    private final UserFolderMappingRepository userFolderMappingRepository;

    public UserFolderMappingCache(RedissonClient redissonClient, UserFolderMappingRepository userFolderMappingRepository) {
        this.redissonClient = redissonClient;
        this.userFolderMappingRepository = userFolderMappingRepository;
    }

    public RBucket<UserFolderMapping> get(UUID key) {
        RBucket<UserFolderMapping> bucket = redissonClient.getBucket(key.toString());
        if (bucket != null) {
            UserFolderMapping userFolderMapping = bucket.get();
            if (userFolderMapping != null) {
                return bucket;
            }
        }
        UserFolderMapping userFolderMapping = userFolderMappingRepository.getByUserId(key);
        put(key.toString(), userFolderMapping);
        return redissonClient.getBucket(key.toString());
    }

    public void put(String bucketName, UserFolderMapping userFolderMapping) {
        redissonClient.getBucket(bucketName).set(userFolderMapping);
    }

    public void delete(String bucketName) {
        redissonClient.getBucket(bucketName).delete();
    }
}
