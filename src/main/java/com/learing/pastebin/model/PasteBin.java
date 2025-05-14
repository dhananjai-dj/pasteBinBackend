package com.learing.pastebin.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paste_bin")
public class PasteBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long          id;
    private long          size;
    private long          views;
    @Column(name = "`key`", columnDefinition = "MEDIUMTEXT")
    private String        key;
    private String        type;
    private String        title;
    private String        content;
    private String        password;
    private String        language;
    private boolean       isPrivate;
    private boolean       isOnceView;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;
}
