package com.example.camouflagegame.Game;

import android.os.Parcel;
import android.os.Parcelable;

public class TankInfo implements Parcelable {
    public int x;
    public int y;
    public boolean isVertical;

    protected TankInfo(Parcel in) {
        x = in.readInt();
        y = in.readInt();
        isVertical = in.readByte() != 0;
    }

    public TankInfo() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeByte((byte) (isVertical ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TankInfo> CREATOR = new Creator<TankInfo>() {
        @Override
        public TankInfo createFromParcel(Parcel in) {
            return new TankInfo(in);
        }

        @Override
        public TankInfo[] newArray(int size) {
            return new TankInfo[size];
        }
    };
}
