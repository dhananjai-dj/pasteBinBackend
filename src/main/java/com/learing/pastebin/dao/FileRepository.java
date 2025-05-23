package com.learing.pastebin.dao;

import com.learing.pastebin.model.File;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    File getByKey(String key);

    @Modifying
    @Transactional
    @Query("UPDATE File f SET f.views = :views WHERE f.id = :id")
    void updateViews(@Param("id") Long id, @Param("views") long views);
}
