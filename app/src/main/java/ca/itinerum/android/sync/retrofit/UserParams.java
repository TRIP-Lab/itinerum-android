package ca.itinerum.android.sync.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by stewjacks on 2016-12-02.
 */

public class UserParams {

	@SerializedName("uuid")
	@Expose
	private String uuid;
	@SerializedName("model")
	@Expose
	private String model;
	@SerializedName("itinerum_version")
	@Expose
	private String itinerumVersion;
	@SerializedName("os")
	@Expose
	private String os;
	@SerializedName("os_version")
	@Expose
	private String osVersion;

	/**
	 *
	 * @return
	 * The uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 *
	 * @param uuid
	 * The uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public UserParams withUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}

	/**
	 *
	 * @return
	 * The model
	 */
	public String getModel() {
		return model;
	}

	/**
	 *
	 * @param model
	 * The model
	 */
	public void setModel(String model) {
		this.model = model;
	}

	public UserParams withModel(String model) {
		this.model = model;
		return this;
	}

	/**
	 *
	 * @return
	 * The dmVersion
	 */
	public String getItinerumVersion() {
		return itinerumVersion;
	}

	/**
	 *
	 * @param itinerumVersion
	 * The itinerum_version
	 */
	public void setItinerumVersion(String itinerumVersion) {
		this.itinerumVersion = itinerumVersion;
	}

	public UserParams withItinerumVersion(String itinerumVersion) {
		this.itinerumVersion = itinerumVersion;
		return this;
	}

	/**
	 *
	 * @return
	 * The os
	 */
	public String getOs() {
		return os;
	}

	/**
	 *
	 * @param os
	 * The os
	 */
	public void setOs(String os) {
		this.os = os;
	}

	public UserParams withOs(String os) {
		this.os = os;
		return this;
	}

	/**
	 *
	 * @return
	 * The osVersion
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 *
	 * @param osVersion
	 * The os_version
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public UserParams withOsVersion(String osVersion) {
		this.osVersion = osVersion;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
