package com.cxmax.remoteservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @describe :
 * @usage :
 * <p>
 * </p>
 * Created by caixi on 17-4-16.
 */

public class User implements Parcelable{
    public int uid;
    public String name;

    public User(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    protected User(Parcel in) {
        uid = in.readInt();
        name = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeString(name);
    }
}
