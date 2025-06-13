package com.example.newsapp;

public class NewsItem {
    public String title;     // Make sure the name matches your Firebase keys
    public String imageurl;  // or imageUrl, but be consistent everywhere
    public String content;
    public String date;

    // Default constructor needed for Firebase
    public NewsItem() {
    }

    // Optional: constructor with parameters
    public NewsItem(String title, String imageurl,String content, String date) {
        this.title = title;
        this.imageurl = imageurl;
        this.content = content;
        this.date = date;
    }
}
