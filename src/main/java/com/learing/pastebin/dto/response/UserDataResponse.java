package com.learing.pastebin.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserDataResponse {
    private String                  name;
    private long                    totalViews;
    private long                    totalPastes;
    private long                    publicPastes;
    private long                    activePastes;
    private long                    folderCount;
    private List<Map<Long, String>> folders;
}
