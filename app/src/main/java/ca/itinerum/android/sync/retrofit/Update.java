package ca.itinerum.android.sync.retrofit;

import android.support.v4.util.ArrayMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Update {

	@SerializedName("cancelledPrompts")
	@Expose
	private List<PromptAnswer> cancelledPrompts = new ArrayList<>();
	@SerializedName("prompts")
	@Expose
	private List<PromptAnswer> prompts = new ArrayList<>();
	@SerializedName("survey_id")
	@Expose
	private long surveyId;
	@SerializedName("survey")
	@Expose
	private ArrayMap<String, Object> survey;
	@SerializedName("uuid")
	@Expose
	private String uuid;
	@SerializedName("coordinates")
	@Expose
	private List<Coordinate> coordinates = new ArrayList<>();

	public long getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(long surveyId) {
		this.surveyId = surveyId;
	}

	public Update withSurveyId(long surveyId) {
		this.surveyId = surveyId;
		return this;
	}

	public ArrayMap<String, Object> getSurvey() {
		return survey;
	}

	public void setSurvey(ArrayMap<String, Object> survey) {
		this.survey = survey;
	}

	public Update withSurvey(ArrayMap<String, Object> survey) {
		this.survey = survey;
		return this;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Update withUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}

	public List<PromptAnswer> getPrompts() {
		return prompts;
	}

	public void setPrompts(List<PromptAnswer> prompts) {
		this.prompts = prompts;
	}

	public Update withPrompts(List<PromptAnswer> prompts) {
		this.prompts = prompts;
		return this;
	}

	public List<PromptAnswer> getCancelledPrompts() {
		return cancelledPrompts;
	}

	public void setCancelledPrompts(List<PromptAnswer> cancelledPrompts) {
		this.cancelledPrompts = cancelledPrompts;
	}

	public Update withCancelledPrompts(List<PromptAnswer> cancelledPrompts) {
		this.cancelledPrompts = cancelledPrompts;
		return this;
	}

	public List<Coordinate> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Coordinate> coordinates) {
		this.coordinates = coordinates;
	}

	public Update withCoordinates(List<Coordinate> coordinates) {
		this.coordinates = coordinates;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


}