package com.learing.pastebin.cache;

import com.learing.pastebin.dao.FileRepository;
import com.learing.pastebin.model.File;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class FileCache {

    private final RedissonClient redissonClient;

    private final FileRepository fileRepository;

    public FileCache(RedissonClient redissonClient, FileRepository fileRepository) {
        this.redissonClient = redissonClient;
        this.fileRepository = fileRepository;
    }

    public RBucket<File> get(String key) {
        RBucket<File> bucket = redissonClient.getBucket(key);
        if (bucket != null) {
            File file = bucket.get();
            if(file != null){
                return bucket;
            }
        }
        File file = fileRepository.getByKey(key);
        put(key, file);
        return redissonClient.getBucket(key);
    }

    public void put(String bucketName, File file) {
        redissonClient.getBucket(bucketName).set(file);
    }

    public void delete(String bucketName) {
        redissonClient.getBucket(bucketName).delete();
    }
}
