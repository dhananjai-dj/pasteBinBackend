package com.learing.pastebin.controller;

import com.learing.pastebin.dto.FolderResponse;
import com.learing.pastebin.dto.UserData;
import com.learing.pastebin.service.UserDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("paste-bin/user")
public class UserController {

    private final UserDataService userDataService;

    public UserController(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    @GetMapping("/all-data")
    public ResponseEntity<UserData> getAllData(@RequestParam UUID userId) {
        return ResponseEntity.ok(userDataService.getUserData(userId));
    }

    @GetMapping("/get-folder")
    public ResponseEntity<FolderResponse> getFolder(@RequestParam long folderId) {
        return ResponseEntity.ok(userDataService.getFolderData(folderId));
    }

    @GetMapping("/update-folderName")
    public ResponseEntity<FolderResponse> updateFolderName(@RequestParam long folderId, @RequestParam String folderName) {
        return ResponseEntity.ok(userDataService.updateFolderName(folderId, folderName));
    }

    @GetMapping("/change-folder")
    public ResponseEntity<String> changeFolder(@RequestParam long fileId, @RequestParam long newFolderId) {
        return ResponseEntity.ok(userDataService.changeFolderData(fileId, newFolderId));
    }
}
