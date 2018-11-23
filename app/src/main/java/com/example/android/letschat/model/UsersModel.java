package com.example.android.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UsersModel implements Parcelable{
    private String name;
    private String status;
    private String image;
    private String id;
    private String thumb_image;

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected UsersModel(Parcel in) {
        name = in.readString();
        status = in.readString();
        image = in.readString();
        id = in.readString();
        thumb_image = in.readString();
    }

    public static final Creator<UsersModel> CREATOR = new Creator<UsersModel>() {
        @Override
        public UsersModel createFromParcel(Parcel in) {
            return new UsersModel(in);
        }

        @Override
        public UsersModel[] newArray(int size) {
            return new UsersModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UsersModel(String name, String status, String image , String id , String thumb_image) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.id = id;
        this.thumb_image = thumb_image;
    }

    public UsersModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(image);
        dest.writeString(id);
        dest.writeString(thumb_image);
    }
}
