package com.learing.pastebin.dao;

import com.learing.pastebin.model.PasteBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasteBinRepository extends JpaRepository<PasteBin, Long> {
    PasteBin getByKey(String key);
}
