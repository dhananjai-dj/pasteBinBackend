package com.learing.pastebin.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long              folderId;
    private String            folderName;
    @CreationTimestamp
    private LocalDateTime     createdAt;
    @UpdateTimestamp
    private LocalDateTime     updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_folder_mapping_id")
    private UserFolderMapping userFolderMapping;
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File>        files = new ArrayList<>();
}
