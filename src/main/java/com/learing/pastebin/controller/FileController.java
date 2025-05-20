package com.learing.pastebin.controller;

import com.learing.pastebin.dto.response.FileResponse;
import com.learing.pastebin.dto.request.FileRequest;
import com.learing.pastebin.dto.response.FileSaveResponse;
import com.learing.pastebin.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paste-bin")
public class FileController {

    private final FileService fileService;

    FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/put-data")
    public ResponseEntity<FileSaveResponse> putData(@RequestBody FileRequest fileRequest) {
        return ResponseEntity.ok(fileService.saveData(fileRequest));
    }

    @PostMapping("/get-data/{key}")
    public ResponseEntity<FileResponse> getData(@RequestBody String password, @PathVariable String key) {
        return new ResponseEntity<>(fileService.getData(key, password), HttpStatus.OK);
    }

}
