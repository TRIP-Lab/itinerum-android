package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class Results {

    @SerializedName("aboutText")
    @Expose
    private String aboutText;
    @SerializedName("termsOfService")
    @Expose
    private String termsOfService;
    @SerializedName("prompt")
    @Expose
    private Prompts prompt = null;
    @SerializedName("survey")
    @Expose
    private List<Survey> survey = null;
    @SerializedName("surveyId")
    @Expose
    private long surveyId;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("user")
    @Expose
    private String user;

    /**
     *
     * @return
     * The about text
     */
    public String getAboutText() {
        return aboutText;
    }

    /**
     *
     * @param aboutText
     * The about text
     */
    public void setAboutText(String aboutText) {
        this.aboutText = aboutText;
    }

    /**
     *
     * @return
     * The Terms Of Service
     */
    public String getTermsOfService() {
        return termsOfService;
    }

    /**
     *
     * @param termsOfService
     * The about text
     */
    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    /**
     *
     * @return
     * The prompts
     */
    public Prompts getPrompt() {
        return prompt;
    }

    /**
     *
     * @param prompt
     * The prompts
     */
    public void setPrompt(Prompts prompt) {
        this.prompt = prompt;
    }

    /**
     *
     * @return
     * The survey
     */
    public List<Survey> getSurvey() {
        return survey;
    }

    /**
     *
     * @param survey
     * The survey
     */
    public void setSurvey(List<Survey> survey) {
        this.survey = survey;
    }

    /**
     *
     * @return
     * The surveyId
     */
    public long getSurveyId() {
        return surveyId;
    }

    /**
     *
     * @param surveyId
     * The survey_id
     */
    public void setSurveyId(long surveyId) {
        this.surveyId = surveyId;
    }

    /**
     *
     * @return
     * The avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     *
     * @param avatar
     * The avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     *
     * @return
     * The user
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}