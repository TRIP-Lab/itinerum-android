package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdateResponse {

	@SerializedName("prompts")
	@Expose
	private String prompts;
	@SerializedName("survey")
	@Expose
	private String survey;
	@SerializedName("coordinates")
	@Expose
	private String coordinates;

	/**
	 *
	 * @return
	 * The prompts
	 */
	public String getPrompts() {
		return prompts;
	}

	/**
	 *
	 * @param prompts
	 * The prompts
	 */
	public void setPrompts(String prompts) {
		this.prompts = prompts;
	}

	/**
	 *
	 * @return
	 * The survey
	 */
	public String getSurvey() {
		return survey;
	}

	/**
	 *
	 * @param survey
	 * The survey
	 */
	public void setSurvey(String survey) {
		this.survey = survey;
	}

	/**
	 *
	 * @return
	 * The coordinates
	 */
	public String getCoordinates() {
		return coordinates;
	}

	/**
	 *
	 * @param coordinates
	 * The coordinates
	 */
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}