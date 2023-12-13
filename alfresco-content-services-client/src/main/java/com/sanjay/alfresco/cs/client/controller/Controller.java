package com.sanjay.alfresco.cs.client.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sanjay.alfresco.cs.client.model.FileMetadata;;
// import com.sanjay.alfresco.cs.client.service.NodesService;
import com.sanjay.alfresco.cs.client.utils.AlfrescoAuthenticator;;
import com.sanjay.alfresco.cs.client.utils.Commons;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/client")
public class Controller{

    @Autowired
    ConfigurableApplicationContext container;

    // @Autowired
    // NodesService nodesService;

    @Autowired
    AlfrescoAuthenticator authenticator;

    @GetMapping("/shutdown")
    private ResponseEntity<String> shutdown(){
        log.info("/shutdown");
        new Thread(()->{
            try{
                Thread.sleep(5000);
                container.close();
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }).start();
        return new ResponseEntity<>("Application shutdown initiated", HttpStatus.OK);
    }

}