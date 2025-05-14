package com.learing.pastebin.dto;

import lombok.Data;

@Data
public class PasteBinDataResponse {
    private String   status;
    private String   content;
    private MetaData metaData;

    @Data
    public static class MetaData {
        private long   views;
        private String type;
        private String title;
        private String language;
        private boolean isPrivate;
        private boolean isOnceView;
    }
}
