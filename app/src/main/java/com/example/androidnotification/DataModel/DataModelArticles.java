package com.example.androidnotification.DataModel;

import com.google.firebase.Timestamp;

public class DataModelArticles {

    private String category, title,ref;
    private int like, comments, view;
    private Timestamp date;

    public DataModelArticles() {
        //need empty
    }

    public DataModelArticles(String category, String title, String ref, int like, int comments, int view, Timestamp date) {
        this.category = category;
        this.title = title;
        this.ref = ref;
        this.like = like;
        this.comments = comments;
        this.view = view;
        this.date = date;
    }


    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getRef() {
        return ref;
    }

    public int getLike() {
        return like;
    }

    public int getComments() {
        return comments;
    }

    public int getView() {
        return view;
    }

    public Timestamp getDate() {
        return date;
    }

}
