package com.sanjay.alfresco.cs.client.utils;

import java.util.Base64;

import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONObject;

public class Commons{

    public static String encodeTicket(String ticket){
        String encodedTicket = Base64.getEncoder().encodeToString(ticket.getBytes());
        return encodedTicket;
    }

    public static String computeFileExtension(JSONObject json, String fileName){
        if(fileName.contains(".")){
            return fileName;
        }
        try{
            String mimeType = json.getJSONObject("content").getString("mimeType");
            String extension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
            return fileName + extension;
        }catch(MimeTypeException e){
            //TODO: read from external props
            return null;
        }
    }
}
