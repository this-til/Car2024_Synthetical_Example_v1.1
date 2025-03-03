package com.benjaminwan.ocrlibrary;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult extends OcrOutput implements Parcelable {
    private double dbNetTime;
    private ArrayList<TextBlock> textBlocks;
    private Bitmap boxImg;
    private double detectTime;
    private String strRes;

    protected OcrResult(Parcel in) {
        dbNetTime = in.readDouble();
        textBlocks = in.createTypedArrayList(TextBlock.CREATOR);
        boxImg = in.readParcelable(Bitmap.class.getClassLoader());
        detectTime = in.readDouble();
        strRes = in.readString();
    }

    public static final Creator<OcrResult> CREATOR = new Creator<>() {
        @Override
        public OcrResult createFromParcel(Parcel in) {
            return new OcrResult(in);
        }

        @Override
        public OcrResult[] newArray(int size) {
            return new OcrResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(dbNetTime);
        dest.writeTypedList(textBlocks);
        dest.writeParcelable(boxImg, flags);
        dest.writeDouble(detectTime);
        dest.writeString(strRes);
    }
}

