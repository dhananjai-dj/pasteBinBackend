package com.learing.pastebin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class FileRequest {
    private long    size;
    private long    duration;
    private long    folderId;
    private String  type;
    private String  title;
    private String  content;
    private String  password;
    private String  language;
    private String  folderName;
    private UUID    userId;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    @JsonProperty("isOnceView")
    private boolean isOnceView;
}
