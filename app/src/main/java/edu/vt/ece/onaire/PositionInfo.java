package edu.vt.ece.onaire;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vedahari on 4/13/2017.
 */

public class PositionInfo implements Parcelable{
    private Location mLoc;
    private double mSpeed;

    /*Interfaces required for implementing as a Parcelable*/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mLoc.writeToParcel(dest,flags);
        dest.writeDouble(mSpeed);
    }

    public static final Creator<PositionInfo> CREATOR = new Creator<PositionInfo>(){
        @Override
        public PositionInfo createFromParcel(Parcel source) {
            return new PositionInfo(source);
        }

        @Override
        public PositionInfo[] newArray(int size) {
            return new PositionInfo[size];
        }
    };


    public PositionInfo() {
        mLoc = new Location("Default");
        mSpeed = 0.0;
    }

    public PositionInfo(Parcel src){
        mLoc = Location.CREATOR.createFromParcel(src);
        mSpeed = src.readDouble();
    }

    public double getSpeed() {
        return mSpeed;
    }

    public void setSpeed(double mSpeed) {
        this.mSpeed = mSpeed;
    }

    public Location getLoc() {
        if (mLoc==null){
            mLoc = new Location("Default");
        }
        return mLoc;
    }

    public void setLoc(Location mLoc) {
        this.mLoc = mLoc;
    }

    @Override
    public String toString() {
        return "PositionInfo{" +
                "mLoc=" + mLoc +
                ", mSpeed=" + mSpeed +
                '}';
    }

    public boolean isLocationValid() {
        if (mLoc!=null){
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object that) {
        if (this==that){
            return true;
        }
        if (this!=that && that==null){
            return false;
        }
        if (!this.isLocationValid() || !((PositionInfo)that).isLocationValid()){
            return false;
        }
        double this_lat = this.getLoc().getLatitude();
        double this_lon = this.getLoc().getLongitude();
        double that_lat = ((PositionInfo)that).getLoc().getLatitude();
        double that_lon = ((PositionInfo)that).getLoc().getLongitude();
        if (Double.compare(this_lat,that_lat)==0 && Double.compare(this_lon, that_lon)==0){
            return true;
        }
        else return false;
    }
}