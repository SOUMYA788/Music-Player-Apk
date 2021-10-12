package com.example.mediaplayer;

public class VideoMusicFiles {
    private String path;
    private String title;
    private String duration;

    public VideoMusicFiles(String path, String title, String duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public VideoMusicFiles() {
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
