package ca.itinerum.android.sync.retrofit;

/**
 * Created by stewjacks on 2017-01-18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Prompts {

	@SerializedName("maxDays")
	@Expose
	private int maxDays = -1;
	@SerializedName("maxPrompts")
	@Expose
	private int maxPrompts = -1;
	@SerializedName("numPrompts")
	@Expose
	private int numPrompts = -1;
	@SerializedName("prompts")
	@Expose
	private List<Prompt> prompts = new ArrayList<>();

	public int getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public int getNumPrompts() {
		return numPrompts;
	}

	public void setNumPrompts(int numPrompts) {
		this.numPrompts = numPrompts;
	}

	public int getMaxPrompts() {
		return maxPrompts;
	}

	public void setMaxPrompts(int maxPrompts) {
		this.maxPrompts = maxPrompts;
	}

	public List<Prompt> getPrompts() {
		return prompts;
	}

	public void setPrompts(List<Prompt> prompts) {
		this.prompts = prompts;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}