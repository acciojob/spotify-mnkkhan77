package com.driver;

import java.util.Date;

public class Album {
    private String title;
    private Date releaseDate;
    private Artist artist;

    public Album(){
    }
    public Album(String title,Artist artist){
        this.title = title;
        this.artist = artist;
    }
    public Album(String title){
        this.title = title;
        this.releaseDate = new Date();
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}