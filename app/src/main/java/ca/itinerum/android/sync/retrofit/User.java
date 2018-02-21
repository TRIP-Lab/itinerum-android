package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by stewjacks on 2016-12-02.
 */

public class User {
	@SerializedName("user")
	@Expose
	private UserParams user;
	@SerializedName("lang")
	@Expose
	private String lang;
	@SerializedName("survey_name")
	@Expose
	private String surveyName;

	/**
	 *
	 * @return
	 * The user
	 */
	public UserParams getUser() {
		return user;
	}

	/**
	 *
	 * @param user
	 * The user
	 */
	public void setUser(UserParams user) {
		this.user = user;
	}

	public User withUser(UserParams user) {
		this.user = user;
		return this;
	}

	/**
	 *
	 * @return
	 * The lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 *
	 * @param lang
	 * The lang
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	public User withLang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 *
	 * @return
	 * The surveyName
	 */
	public String getSurveyName() {
		return surveyName;
	}

	/**
	 *
	 * @param surveyName
	 * The survey_name
	 */
	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public User withSurveyName(String surveyName) {
		this.surveyName = surveyName;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
