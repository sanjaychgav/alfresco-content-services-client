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


}