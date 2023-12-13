package com.sanjay.alfresco.cs.client.utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.sanjay.alfresco.cs.client.constants.Constants;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PreDestroy;

@Slf4j
@Component
public class AlfrescoAuthenticator{

    @Autowired
    RestTemplate restTemplate;

    @Value("${alfresco-user}")
    private String user;
    @Value("${alfresco-password}")
    private String password;
    @Value("${alfresco-base-url}")
    private String baseUrl;

    private String url;
    private String ticket;

    private String getUrl(){
        if(url==null){
            return baseUrl + "/" + Constants.PATH_AUTHENTICATION; 
        }else{
            return url;
        }
    }

    public String getTicket(){
        if(!validateTicket()){
            createTicket();
        }
        return ticket;
    }

    private void createTicket(){
        String url = getUrl();
        JSONObject json = new JSONObject();
        json.put("userId", this.user);
        json.put("password", this.password);
        try{
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<String>(json.toString()), String.class);
            JSONObject respJson;
            if(response.getBody()!=null && (respJson=new JSONObject(response.getBody()))!=null && respJson.has("entry")){
                String userId = respJson.getJSONObject("entry").getString("userId");
                if(!userId.equals(this.user)){
                    //TODO: retry
                }
                String ticket = respJson.getJSONObject("entry").getString("id");
                this.ticket = ticket;
            }else{
                //TODO: log and retry 
            }
        }catch(Exception e){
            //TODO: retry
        }
    }

    private boolean validateTicket(){
        if(ticket==null){
            return false;
        }else{
            String url = getUrl() + "/-me-";
            String encodedTicket = Commons.encodeTicket(ticket);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Basic ".concat(encodedTicket));
            HttpEntity<String> request = new HttpEntity<String>(headers);
            try{
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
                JSONObject json;
                if(response.getBody()!=null && (json = new JSONObject(response.getBody()))!=null && json.has("entry")){
                    String ticket = json.getJSONObject("entry").getString("id");
                    return ticket.equals(this.ticket);                    
                }
            }catch(Exception e){
                return false;
            }
            return false;
        }
    }

    private boolean deleteTicket(){
        if(ticket==null){
            return false;
        }else{
            String url = getUrl() + "/-me-";
            String encodedTicket = Commons.encodeTicket(this.ticket);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Basic ".concat(encodedTicket));
            HttpEntity<String> request = new HttpEntity<String>(headers);
            try{
                restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
                ticket = null;
                return true;
            }catch(Exception e){
                return false;
            }
        }
    }

    @PreDestroy
    public void cleanupJob(){
        log.info("Shutdown initiated!!!");
        deleteTicket();
    }

}