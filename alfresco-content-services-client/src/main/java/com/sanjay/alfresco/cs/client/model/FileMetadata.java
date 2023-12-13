package com.sanjay.alfresco.cs.client.model;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class FileMetadata{

    private String nodeId;
    private String name;
    private String nodeType;
    private String filePath;
    private boolean overwrite;
    private boolean majorVersion;

    public String getNodeId(){
        return nodeId;
    }

    public void setNodeId(String nodeId){
        this.nodeId = nodeId;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getNodeType(){
        return nodeType;
    }

    public void setNodeType(String nodeType){
        this.nodeType = nodeType;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public boolean isOverwrite(){
        return overwrite;
    }

    public void setOverwrite(boolean overwrite){
        this.overwrite = overwrite;
    }

    public boolean isMajorVersion(){
        return majorVersion;
    }

    public void setMajorVersion(boolean majorVersion){
        this.majorVersion = majorVersion;
    }

    //Alfresco Specific Metadata

    private String author;
    private String title;
    private String description;
    private Map<String, String> properties;

    public FileMetadata(){
        properties = new HashMap<>();
    }

    public Map<String, String> getProperties(){
        return properties;
    }

    public String getAuthor(){
        return author;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public void setAuthor(String author){
        this.author=author;
        properties.put("cm:author",author);
    }

    public void setTitle(String title){
        this.title=title;
        properties.put("cm:title",title);
    }

    public void setDescription(String description){
        this.description=description;
        properties.put("cm:description",description);
    }

    public String getPayload(){
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("nodeType", nodeType);
        json.put("properties", properties);
        return json.toString();
    }

}
