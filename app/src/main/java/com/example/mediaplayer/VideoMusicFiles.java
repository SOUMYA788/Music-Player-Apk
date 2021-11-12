package com.example.mediaplayer;

public class VideoMusicFiles {
    private int ID;
    private String path;
    private String title;
    private int duration;
    private int size;

    public VideoMusicFiles(int ID, String path, String title, int duration, int size) {
        this.ID = ID;
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.size = size;
    }

    public VideoMusicFiles() {
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
