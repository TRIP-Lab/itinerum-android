package ca.itinerum.android.sync.retrofit;

/**
 * Created by stewjacks on 2017-01-23.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class LocationUserSurveyAnswer {

	@SerializedName("latitude")
	@Expose
	private double latitude;
	@SerializedName("longitude")
	@Expose
	private double longitude;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public LocationUserSurveyAnswer() {
	}

	/**
	 *
	 * @param longitude
	 * @param latitude
	 */
	public LocationUserSurveyAnswer(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public LocationUserSurveyAnswer withLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public LocationUserSurveyAnswer withLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}

	public LatLng toLatLng() {
		return new LatLng(latitude, longitude);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
