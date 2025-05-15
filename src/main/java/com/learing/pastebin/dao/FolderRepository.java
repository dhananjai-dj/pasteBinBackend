package com.learing.pastebin.dao;

import com.learing.pastebin.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    Folder findByFolderId(long folderId);
}
