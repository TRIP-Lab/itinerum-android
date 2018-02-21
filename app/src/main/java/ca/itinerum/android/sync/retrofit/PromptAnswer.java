package ca.itinerum.android.sync.retrofit;

/**
 * Created by stewjacks on 2017-01-27.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.UUID;

@Entity(tableName = "prompts")
public class PromptAnswer implements Comparable<PromptAnswer> {

	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "_id")
	private transient long mId;

	@ColumnInfo(name = "uploaded")
	private transient boolean mUploaded;

	@Ignore
	@SerializedName("answer")
	@Expose
	private ArrayList<String> mAnswer;

	@NonNull
	@ColumnInfo(name = "answer")
	private String mJsonAnswer = "[]";

	@NonNull
	@ColumnInfo(name = "prompt")
	@SerializedName("prompt")
	@Expose
	private String mPrompt = "";

	@ColumnInfo(name = "latitude")
	@SerializedName("latitude")
	@Expose
	private double mLatitude;

	@ColumnInfo(name = "longitude")
	@SerializedName("longitude")
	@Expose
	private double mLongitude;

	@NonNull
	@ColumnInfo(name = "recorded_at")
	@SerializedName("recorded_at")
	@Expose
	private String mRecordedAt = "";

	@NonNull
	@ColumnInfo(name = "displayed_at")
	@SerializedName("displayed_at")
	@Expose
	private String mDisplayedAt = "";

	@NonNull
	@ColumnInfo(name = "uuid")
	@SerializedName("uuid")
	@Expose
	private String mUuid;

	@NonNull
	@ColumnInfo(name = "prompt_num")
	@SerializedName("prompt_num")
	@Expose
	private int mPromptNumber;

	@NonNull
	@ColumnInfo(name = "cancelled")
	@Expose
	private boolean mCancelled;

	public PromptAnswer(long id, boolean uploaded, @NonNull String jsonAnswer, @NonNull String prompt, double latitude, double longitude, @NonNull String recordedAt, @NonNull String displayedAt, @NonNull String uuid, int promptNumber, boolean cancelled) {
		mId = id;
		mUploaded = uploaded;
		mJsonAnswer = jsonAnswer;
		mPrompt = prompt;
		mLatitude = latitude;
		mLongitude = longitude;
		mRecordedAt = recordedAt;
		mDisplayedAt = displayedAt;
		mUuid = uuid;
		mPromptNumber = promptNumber;
		mCancelled = cancelled;

		if (!(StringUtils.isBlank(mJsonAnswer))) {
			mAnswer = getAnswer();
		}
	}

	@Ignore
	public PromptAnswer() {
		mUuid = UUID.randomUUID().toString();
	}

	@NonNull
	public String getJsonAnswer() {
		return mJsonAnswer;
	}

	public PromptAnswer withJsonAnswer(String jsonAnswer) {
		mJsonAnswer = jsonAnswer;
		if (!(StringUtils.isBlank(mJsonAnswer))) {
			mAnswer = getAnswer();
		}
		return this;
	}

	@Ignore
	public ArrayList<String> getAnswer() {
		return (new Gson()).fromJson(mJsonAnswer, new TypeToken<ArrayList<String>>() {}.getType());
	}

	public PromptAnswer withAnswer(ArrayList<String> answer) {
		mAnswer = answer;
		if (mAnswer != null) mJsonAnswer = new Gson().toJson(mAnswer);
		return this;
	}

	public PromptAnswer withAnswer(String answer) {
		mAnswer = new ArrayList<>();
		mAnswer.add(answer);
		mJsonAnswer = new Gson().toJson(mAnswer);
		return this;
	}

	@NonNull
	public String getPrompt() {
		return mPrompt;
	}

	public void setPrompt(@NonNull String prompt) {
		mPrompt = prompt;
	}

	public PromptAnswer withPrompt(@NonNull String prompt) {
		mPrompt = prompt;
		return this;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public PromptAnswer withLatitude(double latitude) {
		mLatitude = latitude;
		return this;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public PromptAnswer withLongitude(double longitude) {
		mLongitude = longitude;
		return this;
	}

	@NonNull
	public String getRecordedAt() {
		return mRecordedAt;
	}

	public void setRecordedAt(@NonNull String recordedAt) {
		mRecordedAt = recordedAt;
	}

	public PromptAnswer withRecordedAt(@NonNull String recordedAt) {
		mRecordedAt = recordedAt;
		return this;
	}

	@NonNull
	public String getDisplayedAt() {
		return mDisplayedAt;
	}

	public void setDisplayedAt(@NonNull String displayedAt) {
		mDisplayedAt = displayedAt;
	}

	public PromptAnswer withDisplayedAt(@NonNull String displayedAt) {
		mDisplayedAt = displayedAt;
		return this;
	}

	public boolean isUploaded() {
		return mUploaded;
	}

	public void setUploaded(boolean uploaded) {
		mUploaded = uploaded;
	}

	public PromptAnswer withUploaded(boolean uploaded) {
		mUploaded = uploaded;
		return this;
	}

	@NonNull
	public String getUuid() {
		return mUuid;
	}

	public void setUuid(@NonNull String uuid) {
		mUuid = uuid;
	}

	public PromptAnswer withUuid(@NonNull String uuid) {
		mUuid = uuid;
		return this;
	}

	@NonNull
	public int getPromptNumber() {
		return mPromptNumber;
	}

	public void setPromptNumber(int promptNumber) {
		mPromptNumber = promptNumber;
	}

	public PromptAnswer withPromptNumber(int number) {
		setPromptNumber(number);
		return this;
	}

	@NonNull
	public boolean isCancelled() {
		return mCancelled;
	}

	public void setCancelled(boolean cancelled) {
		mCancelled = cancelled;
	}

	public PromptAnswer withCancelled(boolean cancelled) {
		setCancelled(cancelled);
		return this;
	}

	/**
	 * Get mId
	 */

	public long getId() {
		return mId;
	}

	/**
	 * Set mId
	 *
	 * @param id
	 */

	public void setId(long id) {
		mId = id;
	}

	@Ignore
	public LatLng getLatLng() {
		return new LatLng(getLatitude(), getLongitude());
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(@NonNull PromptAnswer promptAnswer) {
		return getId() > promptAnswer.getId() ? 1 : -1;
	}
}