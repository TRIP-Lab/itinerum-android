package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateResponse {

	@SerializedName("status")
	@Expose
	private String status;
	@SerializedName("results")
	@Expose
	private Results results = null;
	@SerializedName("status_code")
	@Expose
	private long status_code;

	/**
	 *
	 * @return
	 * The status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 *
	 * @param status
	 * The status
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**
	 *
	 * @return
	 * The surveyResponse
	 */
	public Results getResults() {
		return results;
	}

	/**
	 *
	 * @param results
	 * The Results
	 */
	public void setResults(Results results) {
		this.results = results;
	}


	/**
	 *
	 * @return
	 * The status_code
	 */
	public long getStatus_code() {
		return status_code;
	}

	/**
	 *
	 * @param status_code
	 * The status_code
	 */
	public void setStatus_code(long status_code) {
		this.status_code = status_code;
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}