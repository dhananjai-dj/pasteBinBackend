package com.learing.pastebin.dao;

import com.learing.pastebin.model.File;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    File getByKey(String key);

    File getById(long id, Limit limit);
}
