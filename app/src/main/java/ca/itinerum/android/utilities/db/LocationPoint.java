package ca.itinerum.android.utilities.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.apache.commons.math3.ml.clustering.Clusterable;

@Entity(tableName = "points")
public class LocationPoint implements Clusterable, ClusterItem {

	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "_id")
	private int mId;
	@ColumnInfo(name = "altitude")
	private double mAltitude;
	@ColumnInfo(name = "longitude")
	private double mLongitude;
	@ColumnInfo(name = "latitude")
	private double mLatitude;
	@NonNull
	@ColumnInfo(name = "timestamp")
	private String mTimestamp = "";
	@ColumnInfo(name = "speed")
	private double mSpeed;
	@ColumnInfo(name = "haccuracy")
	private double mHaccuracy;
	@ColumnInfo(name = "vaccuracy")
	private double mVaccuracy;
	@ColumnInfo(name = "activity")
	private int mActivity;

	public LocationPoint(double mAltitude, double mLatitude, double mLongitude, @NonNull String mTimestamp, double mSpeed, double mHaccuracy, double mVaccuracy, int mActivity){
    	this.mAltitude = mAltitude;
    	this.mLongitude = mLongitude;
    	this.mLatitude = mLatitude;
    	this.mTimestamp = mTimestamp;
    	this.mSpeed = mSpeed;
    	this.mHaccuracy = mHaccuracy;
    	this.mVaccuracy = mVaccuracy;
		this.mActivity = mActivity;
    }

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public void setSpeed(double speed) {
		mSpeed = speed;
	}

	public void setHaccuracy(double haccuracy) {
		mHaccuracy = haccuracy;
	}

	public void setVaccuracy(double vaccuracy) {
		mVaccuracy = vaccuracy;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	@NonNull
	public String getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(@NonNull String timestamp) {
		mTimestamp = timestamp;
	}

	public double getSpeed() {
		return mSpeed;
	}

	public void setSpeed(float speed) {
		mSpeed = speed;
	}

	public double getHaccuracy() {
		return mHaccuracy;
	}

	public void setHaccuracy(float hAccuracy) {
		mHaccuracy = hAccuracy;
	}

	public double getVaccuracy() {
		return mVaccuracy;
	}

	public void setVaccuracy(float vAccuracy) {
		mVaccuracy = vAccuracy;
	}

	public int getActivity() {
		return mActivity;
	}
    
    public void setActivity(int activity) {
        mActivity = activity;
    }

	/**
	 * Returns the distance (in meters) between this point
	 * and the passed point.
	 *
	 * @param p
	 * @return
	 */
	@Ignore
	public double getDistanceFromPoint(LocationPoint p) {

		float[] results = new float[3];

		Location.distanceBetween(mLatitude, mLongitude, p.mLatitude, p.mLongitude, results);

		return results[0];
	}

	/**
     * Returns the perpendicular
     * distance (in km) between this point and the line
     * passing through p1 and p2
     *
     * @param p1
     * @param p2
     * @return
     */
	@Ignore
    public double getPerpendicularDistanceFromLine(LocationPoint p1, LocationPoint p2)
    {
        double area = 0.0, bottom = 0.0, height = 0.0;
        area = Math.abs(
                          (
                           p1.getLatitude() * p2.getLongitude()
                           + p2.getLatitude() * getLongitude()
                           + getLatitude() * p1.getLongitude()
                           - p2.getLatitude() * p1.getLongitude()
                           - getLatitude() * p2.getLongitude()
                           - p1.getLatitude() * getLongitude()
                           ) / 2.0);

        bottom = Math.sqrt(Math.pow(p1.getLatitude() - p2.getLatitude(), 2) +
                             Math.pow(p1.getLongitude() - p2.getLongitude(), 2));

        height = area / bottom * 2.0;

        return height;
    }

	@Ignore
	@Override
	public double[] getPoint() {
		double[] d = {mLatitude, mLongitude};
		return d;
	}

	@Ignore
	@Override
	public LatLng getPosition() {
		return new LatLng(mLatitude, mLongitude);
	}
}
