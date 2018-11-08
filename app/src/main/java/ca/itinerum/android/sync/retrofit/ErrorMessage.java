package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ErrorMessage {

	@SerializedName("status")
	@Expose
	private String mStatus;

	@SerializedName("errors")
	@Expose
	private String[] mErrors;

	@SerializedName("type")
	@Expose
	private String mType;

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		mStatus = status;
	}

	public String[] getErrors() {
		return mErrors;
	}

	public void setErrors(String[] errors) {
		mErrors = errors;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
