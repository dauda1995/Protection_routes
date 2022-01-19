package com.example.ptapp.asset;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Asset implements Parcelable {
    public String uid;
    public String asset;
    public String location;
    public LatLng latLng;
    public String feeder;
    public String imageUrl;
    public Double latitude;
    public Double longitude;
    public String status;
    public String insulation;
    public String recommendation;


    public Asset() {
    }


    protected Asset(Parcel in) {
        uid = in.readString();
        asset = in.readString();
        location = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        feeder = in.readString();
        imageUrl = in.readString();
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
        status = in.readString();
        insulation = in.readString();
        recommendation = in.readString();
    }

    public static final Creator<Asset> CREATOR = new Creator<Asset>() {
        @Override
        public Asset createFromParcel(Parcel in) {
            return new Asset(in);
        }

        @Override
        public Asset[] newArray(int size) {
            return new Asset[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(asset);
        dest.writeString(location);
        dest.writeParcelable(latLng, flags);
        dest.writeString(feeder);
        dest.writeString(imageUrl);
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
        dest.writeString(status);
        dest.writeString(insulation);
        dest.writeString(recommendation);
    }
}

