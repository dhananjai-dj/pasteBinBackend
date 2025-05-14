package com.learing.pastebin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PasteBinRequest {
    private long    size;
    private long    duration;
    private String  type;
    private String  title;
    private String  content;
    private String  password;
    private String  language;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    @JsonProperty("isOnceView")
    private boolean isOnceView;
}
