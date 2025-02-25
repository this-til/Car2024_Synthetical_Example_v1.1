package com.benjaminwan.ocrlibrary;

import android.os.Parcel;
import android.os.Parcelable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class TextBlock implements Parcelable {
    private ArrayList<Point> boxPoint;
    private float boxScore;
    private int angleIndex;
    private float angleScore;
    private double angleTime;
    private String text;
    private float[] charScores;
    private double crnnTime;
    private double blockTime;

    protected TextBlock(Parcel in) {
        boxPoint = in.createTypedArrayList(Point.CREATOR);
        boxScore = in.readFloat();
        angleIndex = in.readInt();
        angleScore = in.readFloat();
        angleTime = in.readDouble();
        text = in.readString();
        charScores = in.createFloatArray();
        crnnTime = in.readDouble();
        blockTime = in.readDouble();
    }

    public static final Creator<TextBlock> CREATOR = new Creator<TextBlock>() {
        @Override
        public TextBlock createFromParcel(Parcel in) {
            return new TextBlock(in);
        }

        @Override
        public TextBlock[] newArray(int size) {
            return new TextBlock[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(boxPoint);
        dest.writeFloat(boxScore);
        dest.writeInt(angleIndex);
        dest.writeFloat(angleScore);
        dest.writeDouble(angleTime);
        dest.writeString(text);
        dest.writeFloatArray(charScores);
        dest.writeDouble(crnnTime);
        dest.writeDouble(blockTime);
    }
}
