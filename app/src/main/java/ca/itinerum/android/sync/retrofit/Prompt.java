package ca.itinerum.android.sync.retrofit;

/**
 * Created by stewjacks on 2017-01-18.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Prompt implements Parcelable {

	@SerializedName("prompt")
	@Expose
	private String prompt;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("choices")
	@Expose
	private List<String> choices = null;

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Prompt withPrompt(String prompt) {
		this.prompt = prompt;
		return this;
	}

	public int getId() {
		return id;
	}

	public void setId(int prompt) {
		this.id = id;
	}

	public Prompt withId(int id) {
		this.id = id;
		return this;
	}

	public List<String> getChoices() {
		return choices;
	}

	public void setChoices(List<String> choices) {
		this.choices = choices;
	}

	public Prompt withChoices(List<String> choices) {
		this.choices = choices;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Prompt createFromParcel(Parcel in) {
			return new Prompt(in);
		}

		public Prompt[] newArray(int size) {
			return new Prompt[size];
		}
	};

	public Prompt(Parcel in) {
		this.choices = new ArrayList<>();
		this.prompt = in.readString();
		this.id = in.readInt();
		in.readStringList(this.choices);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.prompt);
		dest.writeInt(this.id);
		dest.writeStringList(this.choices);
	}


}