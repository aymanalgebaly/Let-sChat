package com.example.android.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatModel implements Parcelable{


    private String date;


    protected ChatModel(Parcel in) {
        date = in.readString();

    }

    public static final Creator<ChatModel> CREATOR = new Creator<ChatModel>() {
        @Override
        public ChatModel createFromParcel(Parcel in) {
            return new ChatModel(in);
        }

        @Override
        public ChatModel[] newArray(int size) {
            return new ChatModel[size];
        }
    };



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ChatModel(String date) {
        this.date = date;

    }

    public ChatModel() {
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