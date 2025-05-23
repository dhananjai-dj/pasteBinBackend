package com.learing.pastebin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "paste_bin")
public class File implements Serializable {
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
    private UUID          userId;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "folderId")
    private Folder        folder;
}
