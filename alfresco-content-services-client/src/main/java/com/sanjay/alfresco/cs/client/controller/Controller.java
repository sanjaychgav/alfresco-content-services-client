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

import com.sanjay.alfresco.cs.client.model.FileMetadata;
import com.sanjay.alfresco.cs.client.service.NodesService;
import com.sanjay.alfresco.cs.client.utils.AlfrescoAuthenticator;
import com.sanjay.alfresco.cs.client.utils.Commons;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/client")
public class Controller{

    @Autowired
    ConfigurableApplicationContext container;

    @Autowired
    NodesService nodesService;

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

    @RequestMapping(value = "/ticket", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> login(){
        String ticket = authenticator.getTicket();
        String encTicket = Commons.encodeTicket(ticket);
        JSONObject json = new JSONObject();
        json.put("Ticket", ticket);
        json.put("Encoded Ticket", "Basic ".concat(encTicket));
        return new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/ticket", method = RequestMethod.DELETE)
    private ResponseEntity<String> logout(){
        boolean flag = authenticator.deleteTicket();
        return new ResponseEntity<>("Ticket deletion ".concat(flag?"success":"failed"), HttpStatus.OK);
    }

    @RequestMapping(value = "/node-info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> getNodeInfo(@RequestParam(name = "nodeId") String nodeId,
        @RequestParam(name = "relativePath", required = false) String relativePath,
        @RequestParam(name = "children", defaultValue = "false") boolean children){
        
        String info;
        if(relativePath!=null){
            //Child node
            info = nodesService.getNodeInfo(nodeId, relativePath);
        }else{
            info = nodesService.getNodeInfo(nodeId, children);
        }
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/node-info", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> updateNodeInfo(@RequestParam(name = "nodeId") String nodeId,
        @RequestParam(name = "relativePath", required = false) String relativePath,
        @RequestBody FileMetadata metadata){

        if(relativePath!=null){
            //Child node
            JSONObject json = new JSONObject(nodesService.getNodeInfo(nodeId, relativePath));
            nodeId = json.getString("id");
        }
        String info = nodesService.updateNodeInfo(nodeId, metadata);
        return new ResponseEntity<>(info, HttpStatus.CREATED);
    }

    @RequestMapping(name = "/node-content", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> getNodeContent(@RequestParam(name = "nodeId") String nodeId,
        @RequestParam(name = "relativePath", required = false) String relativePath,
        @RequestParam(name = "downloadPath", required = false) String downloadPath){

        JSONObject json = new JSONObject(nodesService.getNodeInfo(nodeId, false));
        if(relativePath!=null){
            //Child node
            json = new JSONObject(nodesService.getNodeInfo(nodeId, relativePath));
            nodeId = json.getString("id");
        }
        byte[] bytes = nodesService.getNodeContent(nodeId, true);
        String fileName = json.getString("name");
        fileName = Commons.computeFileExtension(json, fileName);
        String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;
        if(downloadPath!=null)
            filePath = downloadPath + "/" + fileName;
        File destFile = new File(filePath);
        try{
            FileUtils.copyToFile(new ByteArrayInputStream(bytes), destFile);
        }catch(IOException e){
            e.printStackTrace();
        }
        return new ResponseEntity<>("File downloaded: ".concat(filePath),HttpStatus.CREATED);
    }

    @RequestMapping(value = "/node-create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> createNode(@RequestBody FileMetadata metadata, 
        @RequestParam(name = "relativePath", required = false, defaultValue = "") String relativePath){
        String response = nodesService.createNode(metadata, relativePath);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/node-delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> deleteNode(@RequestParam(name = "nodeId") String nodeId,
        @RequestParam(name = "relativePath", required = false) String relativePath){

        if(relativePath!=null){
            //Child node
            JSONObject json = new JSONObject(nodesService.getNodeInfo(nodeId, relativePath));
            nodeId = json.getString("id");
        }
        boolean flag = nodesService.deleteNode(nodeId);
        if(flag)
            return new ResponseEntity<>(HttpStatus.valueOf(204));
        else
            return new ResponseEntity<String>(HttpStatus.BAD_GATEWAY);
    }

}