package com.example.androidnotification.DataModel;

public class DataModelAlgorithm {

    private String uid, category;
    private int like, view, comments;

    public DataModelAlgorithm() {
        //need empty
    }

    public DataModelAlgorithm(String uid, String category, int like, int view, int comments) {
        this.uid = uid;
        this.category = category;
        this.like = like;
        this.view = view;
        this.comments = comments;
    }

    public String getUid() {
        return uid;
    }

    public String getCategory() {
        return category;
    }

    public int getLike() {
        return like;
    }

    public int getView() {
        return view;
    }

    public int getComments() {
        return comments;
    }
}
