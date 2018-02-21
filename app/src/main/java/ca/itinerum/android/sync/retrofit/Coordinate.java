package ca.itinerum.android.sync.retrofit;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Coordinate {

	@ColumnInfo(name = "vaccuracy")
	@SerializedName("v_accuracy")
	@Expose
	private double mVAccuracy;

	@ColumnInfo(name = "activity")
	@SerializedName("mode_detected")
	@Expose
	private int mModeDetected;

	@ColumnInfo(name = "haccuracy")
	@SerializedName("h_accuracy")
	@Expose
	private double mHAccuracy;

	@ColumnInfo(name = "latitude")
	@SerializedName("latitude")
	@Expose
	private double mLatitude;

	@ColumnInfo(name = "timestamp")
	@SerializedName("timestamp")
	@Expose
	private String mTimestamp;

	@ColumnInfo(name = "speed")
	@SerializedName("speed")
	@Expose
	private double mSpeed;

	@ColumnInfo(name = "longitude")
	@SerializedName("longitude")
	@Expose
	private double mLongitude;

	//these exist on the backend but have no Android result
	@Ignore
	@SerializedName("acceleration_y")
	@Expose
	private double mAccelerationY;

	@Ignore
	@SerializedName("acceleration_x")
	@Expose
	private double mAccelerationX;

	@Ignore
	@SerializedName("acceleration_z")
	@Expose
	private double mAccelerationZ;

	/**
	 *
	 * @return
	 * The vAccuracy
	 */
	public double getVAccuracy() {
		return mVAccuracy;
	}

	/**
	 *
	 * @param vAccuracy
	 * The v_accuracy
	 */
	public void setVAccuracy(double vAccuracy) {
		mVAccuracy = vAccuracy;
	}


	public Coordinate withVAccuracy(double vAccuracy) {
		mVAccuracy = vAccuracy;
		return this;
	}

	/**
	 *
	 * @return
	 * The modeDetected
	 */
	public int getModeDetected() {
		return mModeDetected;
	}

	/**
	 *
	 * @param modeDetected
	 * The mode_detected
	 */
	public void setModeDetected(int modeDetected) {
		mModeDetected = modeDetected;
	}

	/**
	 *
	 * @return
	 * The accelerationY
	 */
	public double getAccelerationY() {
		return mAccelerationY;
	}

	/**
	 *
	 * @param accelerationY
	 * The acceleration_y
	 */
	public void setAccelerationY(float accelerationY) {
		mAccelerationY = accelerationY;
	}

	/**
	 *
	 * @return
	 * The hAccuracy
	 */
	public double getHAccuracy() {
		return mHAccuracy;
	}

	/**
	 *
	 * @param hAccuracy
	 * The h_accuracy
	 */
	public void setHAccuracy(double hAccuracy) {
		mHAccuracy = hAccuracy;
	}

	/**
	 *
	 * @return
	 * The accelerationX
	 */
	public double getAccelerationX() {
		return mAccelerationX;
	}

	/**
	 *
	 * @param accelerationX
	 * The acceleration_x
	 */
	public void setAccelerationX(double accelerationX) {
		mAccelerationX = accelerationX;
	}

	/**
	 *
	 * @return
	 * The latitude
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 *
	 * @param latitude
	 * The latitude
	 */
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	/**
	 *
	 * @return
	 * The timestamp
	 */
	public String getTimestamp() {
		return mTimestamp;
	}

	/**
	 *
	 * @param timestamp
	 * The timestamp
	 */
	public void setTimestamp(String timestamp) {
		mTimestamp = timestamp;
	}

	/**
	 *
	 * @return
	 * The speed
	 */
	public double getSpeed() {
		return mSpeed;
	}

	/**
	 *
	 * @param speed
	 * The speed
	 */
	public void setSpeed(double speed) {
		mSpeed = speed;
	}

	/**
	 *
	 * @return
	 * The longitude
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 *
	 * @param longitude
	 * The longitude
	 */
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	/**
	 *
	 * @return
	 * The accelerationZ
	 */
	public double getAccelerationZ() {
		return mAccelerationZ;
	}

	/**
	 *
	 * @param accelerationZ
	 * The acceleration_z
	 */
	public void setAccelerationZ(double accelerationZ) {
		mAccelerationZ = accelerationZ;
	}

	public Coordinate withAccelerationZ(double accelerationZ) {
		mAccelerationZ = accelerationZ;
		return this;
	}

	public Coordinate withModeDetected(int modeDetected) {
		mModeDetected = modeDetected;
		return this;
	}

	public Coordinate withAccelerationY(double accelerationY) {
		mAccelerationY = accelerationY;
		return this;
	}

	public Coordinate withHAccuracy(double hAccuracy) {
		mHAccuracy = hAccuracy;
		return this;
	}

	public Coordinate withAccelerationX(double accelerationX) {
		mAccelerationX = accelerationX;
		return this;
	}

	public Coordinate withLatitude(double latitude) {
		mLatitude = latitude;
		return this;
	}

	public Coordinate withTimestamp(String timestamp) {
		mTimestamp = timestamp;
		return this;
	}

	public Coordinate withSpeed(double speed) {
		mSpeed = speed;
		return this;
	}

	public Coordinate withLongitude(double longitude) {
		mLongitude = longitude;
		return this;
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
