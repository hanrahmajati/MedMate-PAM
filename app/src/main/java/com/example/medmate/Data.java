package com.example.medmate;

public class Data {
    private String dataId;
    private String title;
    private String description;
    private String imageUrl;

    public Data() {
        // Default constructor required for calls to DataSnapshot.getValue(Data.class)
    }

    public Data(String dataId, String title, String description, String imageUrl) {
        this.dataId = dataId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}


