package com.learing.pastebin.controller;

import com.learing.pastebin.dto.PasteBinDataResponse;
import com.learing.pastebin.dto.PasteBinRequest;
import com.learing.pastebin.dto.PasteBinResponse;
import com.learing.pastebin.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paste-bin")
public class PasteBinController {

    @Autowired
    private DataService dataService;

    @PostMapping("/put-data")
    public ResponseEntity<PasteBinResponse> putData(@RequestBody PasteBinRequest pasteBinRequest) {
        return ResponseEntity.ok(dataService.saveData(pasteBinRequest));
    }

    @PostMapping("/get-data/{key}")
    public ResponseEntity<PasteBinDataResponse> getData(@RequestBody String password, @PathVariable String key) {
        return new ResponseEntity<>(dataService.getData(key, password), HttpStatus.OK);
    }

}
