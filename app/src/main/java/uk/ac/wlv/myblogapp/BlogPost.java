package uk.ac.wlv.myblogapp;

import java.sql.Timestamp;

public class BlogPost {

    /**
     * This class store the all details of Blog post
     * including getters and setters
     * */

    public String user_id, image, desc, image_thum, date , time;
   // public Timestamp timestamp;

    public BlogPost(){


    }

    public BlogPost(String user_id, String image_url, String desc, String image_thum,String date, String time) {
        this.user_id = user_id;
        this.image = image_url;
        this.desc = desc;
        this.image_thum = image_thum;
        this.date = date;
        this.time = time;
        //this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image_url) {
        this.image = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_thum() {
        return image_thum;
    }

    public void setImage_thum(String image_thum) {
        this.image_thum = image_thum;
    }

  /*  public Timestamp getTimestamp() {
       return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }*/
}
