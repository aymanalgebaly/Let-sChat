package com.example.android.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendsModel implements Parcelable{


    private String date;


    protected FriendsModel(Parcel in) {
        date = in.readString();

    }

    public static final Creator<FriendsModel> CREATOR = new Creator<FriendsModel>() {
        @Override
        public FriendsModel createFromParcel(Parcel in) {
            return new FriendsModel(in);
        }

        @Override
        public FriendsModel[] newArray(int size) {
            return new FriendsModel[size];
        }
    };



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public FriendsModel(String date) {
        this.date = date;

    }

    public FriendsModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);

    }
}