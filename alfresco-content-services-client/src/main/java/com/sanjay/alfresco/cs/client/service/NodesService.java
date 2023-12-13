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
            if(response.getBody()!=null && (json=new JSONObject(response.getBody()))!=null && json.has("entry")){
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

}