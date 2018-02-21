package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Survey {

	@SerializedName("fields")
	@Expose
	private Fields fields;
	@SerializedName("prompt")
	@Expose
	private String prompt;
	@SerializedName("colName")
	@Expose
	private String colName;
	@SerializedName("id")
	@Expose
	private int id;

	public Fields getFields() {
		return fields;
	}

	public void setFields(Fields fields) {
		this.fields = fields;
	}

	public Survey withFields(Fields fields) {
		this.fields = fields;
		return this;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Survey withPrompt(String prompt) {
		this.prompt = prompt;
		return this;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public Survey withColName(String colName) {
		this.colName = colName;
		return this;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Survey withId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


}