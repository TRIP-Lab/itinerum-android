package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class Fields {

	@SerializedName("choices")
	@Expose
	private List<String> choices = null;
	@SerializedName("latitude")
	@Expose
	private String latitude;
	@SerializedName("longitude")
	@Expose
	private String longitude;

	public List<String> getChoices() {
		return choices;
	}

	public void setChoices(List<String> choices) {
		this.choices = choices;
	}

	public Fields withChoices(List<String> choices) {
		this.choices = choices;
		return this;
	}

	public Object getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public Fields withLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}

	public Object getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public Fields withLongitude(String longitude) {
		this.longitude = longitude;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}