package com.learing.pastebin.service;

import com.learing.pastebin.dao.PasteBinRepository;
import com.learing.pastebin.dto.PasteBinDataResponse;
import com.learing.pastebin.dto.PasteBinRequest;
import com.learing.pastebin.dto.PasteBinResponse;
import com.learing.pastebin.model.PasteBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private PasteBinRepository pasteBinRepository;

    @Autowired
    private UtilService utilService;

    public PasteBinResponse saveData(PasteBinRequest pasteBinRequest) {
        PasteBinResponse pasteBinResponse = new PasteBinResponse();
        pasteBinResponse.setStatus("failure");
        try {
            PasteBin pasteBin = utilService.mapPasteBin(pasteBinRequest);
            pasteBinRepository.save(pasteBin);
            pasteBinResponse.setStatus("success");
            pasteBinResponse.setUrl("http://localhost:8080/paste-bin/get-data/" + pasteBin.getKey());
        } catch (Exception e) {
            logger.error("Error while saving data", e);
        }
        return pasteBinResponse;
    }

    public PasteBinDataResponse getData(String key, String password) {
        PasteBinDataResponse pasteBinDataResponse = new PasteBinDataResponse();
        try {
            PasteBin pasteBin = pasteBinRepository.getByKey(key);
            if (pasteBin != null && utilService.validatePassword(password, pasteBin.getPassword())) {
                LocalDateTime expiredAt = pasteBin.getExpiredAt();
                pasteBin.setViews(pasteBin.getViews() + 1);
                if (expiredAt.isAfter(LocalDateTime.now())) {
                    pasteBinDataResponse.setContent(pasteBin.getContent());
                    if (pasteBin.isOnceView()) {
                        pasteBinRepository.delete(pasteBin);
                    } else {
                        pasteBinRepository.save(pasteBin);
                    }
                    utilService.mapPasteBinMetaData(pasteBin, pasteBinDataResponse);
                } else {
                    pasteBinDataResponse.setStatus("Paste bin expired");
                }
            } else {
                pasteBinDataResponse.setStatus("Invalid password");
            }

        } catch (Exception e) {
            logger.error("Error while getting data {}", e.getMessage());
        }
        return pasteBinDataResponse;
    }
}
