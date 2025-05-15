package com.learing.pastebin.dao;

import com.learing.pastebin.model.UserFolderMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserFolderMappingRepository extends JpaRepository<UserFolderMapping, Long> {
    UserFolderMapping getByUserId(UUID userId);
}
