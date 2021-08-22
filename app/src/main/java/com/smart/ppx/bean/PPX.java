package com.smart.ppx.bean;

public class PPX {
    private int width;
    private int height;
    private String url;
    private String cover;

    public PPX() {
    }

    public PPX(int width, int height, String url, String cover) {
        this.width = width;
        this.height = height;
        this.url = url;
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
