package com.learing.pastebin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_folder_mapping")
@Data
public class UserFolderMapping implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long         id;
    private UUID         userId;
    private long         folderId;
    private String       userName;
    @OneToMany(mappedBy = "userFolderMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>();

    @Override
    public String toString() {
        return "UserFolderMapping{id=" + id + ", userName='" + userName + "'}";
    }

}
