package com.learing.pastebin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FolderResponse {
    private long          folderId;
    private String        name;
    private String        status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String>  fileLinks;
}
