package com.example.ptapp.Joints;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Joints implements Parcelable {

    public String Imageid;
    public String time;
    public String location;
    public String type;
    public Double latitude;
    public Double longitude;
    public String feeder;


    public Joints() {
    }

    public Joints(String imageid, String time, String location, String type, Double latitude, Double longitude, String feeder) {
        Imageid = imageid;
        this.time = time;
        this.location = location;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.feeder = feeder;
    }

    protected Joints(Parcel in) {
        Imageid = in.readString();
        time = in.readString();
        location = in.readString();
        type = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        feeder = in.readString();
    }

    public static final Creator<Joints> CREATOR = new Creator<Joints>() {
        @Override
        public Joints createFromParcel(Parcel in) {
            return new Joints(in);
        }

        @Override
        public Joints[] newArray(int size) {
            return new Joints[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Imageid);
        dest.writeString(time);
        dest.writeString(location);
        dest.writeString(type);
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeString(feeder);
    }
}
