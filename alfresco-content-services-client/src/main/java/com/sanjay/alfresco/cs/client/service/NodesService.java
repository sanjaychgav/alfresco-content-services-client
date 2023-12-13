package com.sanjay.alfresco.cs.client.service;

import java.util.Map.Entry;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.sanjay.alfresco.cs.client.constants.Constants;
import com.sanjay.alfresco.cs.client.model.FileMetadata;
import com.sanjay.alfresco.cs.client.utils.AlfrescoAuthenticator;
import com.sanjay.alfresco.cs.client.utils.Commons;

@Service
public class NodesService{

    @Autowired
    AlfrescoAuthenticator authenticator;

    @Autowired
    RestTemplate restTemplate;

    @Value("${alfresco-base-url}")
    private String baseUrl;

    private String url;

    private HttpHeaders headers;

    private String getUrl(){
        if(this.url==null){
            String url = baseUrl + "/" + Constants.PATH_NODES;
            this.url = url;
            return url;
        }else{
            return this.url;
        }
    }

    private HttpHeaders getAuthenticatedHeaders(){
        if(this.headers!=null){
            return this.headers;
        }
        HttpHeaders headers = new HttpHeaders();
        String ticket = authenticator.getTicket();
        String encodedTicket = Commons.encodeTicket(ticket);
        headers.add(HttpHeaders.AUTHORIZATION, "Basic ".concat(encodedTicket));
        this.headers = headers;
        return headers;
    }

    public String getNodeInfo(String nodeId, boolean listChildren){
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/").append(nodeId);
        if(listChildren)
            url.append("/children");
        return getNodeInformation(url);
    }

    public String getNodeInfo(String parentNodeId, String relativePath){
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/").append(parentNodeId).append("?relativePath=").append(relativePath);
        return getNodeInformation(url);
    }

    private String getNodeInformation(StringBuffer url){
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(url.toString(), HttpMethod.GET, request, String.class);
            JSONObject json;
            if(response.getBody()!=null && (json = new JSONObject(response.getBody()))!=null && json.has("entry")){
                JSONObject info = json.getJSONObject("entry");
                return info.toString();
            }else{
                //TODO: log error and retry and throw error
            }            
        }catch(Exception e){
            //TODO: log error retry
            return null;
        }
        return null;
    }

    public byte[] getNodeContent(String nodeId, boolean attachment){
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/")
        .append(nodeId)
        .append("?attachment=")
        .append(attachment);
        return getNodeContent(url);
    }

    public byte[] getNodeContent(String parentNodeId, String relativePath, boolean attachment){
        JSONObject json = new JSONObject(getNodeInfo(parentNodeId, relativePath));
        String nodeId = json.getString("id");
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/")
        .append(nodeId)
        .append("?attachment=")
        .append(attachment);
        return getNodeContent(url);
    }

    private byte[] getNodeContent(StringBuffer url){
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        try{
            ResponseEntity<byte[]> response = restTemplate.exchange(url.toString(), HttpMethod.GET, request, byte[].class);
            byte[] bytes;
            if((bytes = response.getBody())!=null && bytes.length>0){
                return bytes;
            }else{
                //TODO: no content
                return null;
            }
        }catch(Exception e){
            //TODO: log error and retry
            return null;
        }
    }

    public String updateNodeInfo(String nodeId, FileMetadata metadata){
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/").append(nodeId);
        String payload = metadata.getPayload();
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(url.toString(), HttpMethod.PUT, request, String.class);
            JSONObject respJson;
            if(response.getBody()!=null && (respJson = new JSONObject(response.getBody()))!=null && respJson.has("entry")){
                JSONObject info = respJson.getJSONObject("entry");
                return info.toString();
            }else{
                //TODO: log error and retry
            }
        }catch(Exception e){
            //TODO: log error and retry
            return null;
        }
        return null;
    }

    public String createNode(FileMetadata metadata, String relativePath){
        StringBuffer url = new StringBuffer(getUrl());
        url.append("/").append(metadata.getNodeId())
        .append( (relativePath!=null && !relativePath.isEmpty()) ? relativePath : "")
        .append("/children");
        HttpHeaders headers = getAuthenticatedHeaders();
        Object request;
        String name = metadata.getName();
        Resource resource;

        if(metadata.getFilePath()!=null && (resource = new FileSystemResource(metadata.getFilePath()))!=null && resource.exists()){
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("filedata", resource);
            name = resource.getFilename();
            builder.part("name", name);
            builder.part("nodeType", metadata.getNodeType());
            builder.part("overwrite", metadata.isOverwrite());
            builder.part("majorVersion", metadata.isMajorVersion());
            for(Entry<String, String> entry: metadata.getProperties().entrySet()){
                builder.part(entry.getKey(), entry.getValue());
            }
            HttpEntity<MultiValueMap<String, HttpEntity<?>>> fileRequest = new HttpEntity<>(builder.build(), headers);
            request = fileRequest;
        }else{
            String body = metadata.getPayload();
            HttpEntity<String> folderRequest = new HttpEntity<>(body, headers);
            request = folderRequest;
        }
        
        try{
            ResponseEntity<String> response = restTemplate.exchange(url.toString(), HttpMethod.POST, (HttpEntity<Object>)request, String.class);
            JSONObject respJson;
            if(response.getBody()!=null && (respJson= new JSONObject())!=null && respJson.has("entry")){
                JSONObject info = respJson.getJSONObject("entry");
                return info.toString();
            }else{
                //TODO: log error and retry
            }
        }catch(Exception e){
            //TODO: log error and retry
        }
        return null;
    }

}