/*******************************************************************
 * @title FLIR THERMAL SDK
 * @file FrameDataHolder.java
 * @Author FLIR Systems AB
 *
 * @brief Container class that holds references to Bitmap images
 *
 * Copyright 2019:    FLIR Systems
 ********************************************************************/

package com.example.ptapp.flir;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.flir.thermalsdk.image.ThermalImage;

public class FrameDataHolder implements Parcelable {

    public Bitmap msxBitmap;
    public Bitmap dcBitmap;
    public ThermalImage thermalImage;

    public FrameDataHolder() {
    }

    public FrameDataHolder(Bitmap msxBitmap) {
        this.msxBitmap = msxBitmap;
    }
    public FrameDataHolder(Bitmap msxBitmap, Bitmap dcBitmap){
        this.msxBitmap = msxBitmap;
        this.dcBitmap = dcBitmap;
    }

    public FrameDataHolder(ThermalImage thermalImage, Bitmap msxBitmap, Bitmap dcBitmap){
        this.msxBitmap = msxBitmap;
        this.dcBitmap = dcBitmap;
        this.thermalImage = thermalImage;
    }

    protected FrameDataHolder(Parcel in) {
        msxBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        dcBitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<FrameDataHolder> CREATOR = new Creator<FrameDataHolder>() {
        @Override
        public FrameDataHolder createFromParcel(Parcel in) {
            return new FrameDataHolder(in);
        }

        @Override
        public FrameDataHolder[] newArray(int size) {
            return new FrameDataHolder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(msxBitmap, flags);
        dest.writeParcelable(dcBitmap, flags);
    }
}
