package ca.itinerum.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.itinerum.android.recording.Session;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.SharedPreferenceManager;

/**
 * Created by stewjacks on 2018-01-30.
 */

public class PromptAnswerGroup {

	private List<PromptAnswer> mPromptAnswers;

	private int mPosition;
	private int mCapacity;
	private final LatLng mLocation;
	private final DateTime mSubmitDate;
	private final String mUUID;


	/** Constructor for existing prompt group
	 *
	 * @param promptAnswers
	 */
	public PromptAnswerGroup(@NonNull List<PromptAnswer> promptAnswers) {
		mPromptAnswers = promptAnswers;
		mLocation = new LatLng(promptAnswers.get(0).getLatitude(), promptAnswers.get(0).getLongitude());
		mSubmitDate = DateTime.parse(mPromptAnswers.get(0).getRecordedAt());
		mUUID = mPromptAnswers.get(0).getUuid();
		mCapacity = promptAnswers.size();
	}

	/** Constructor for new prompt group manually
	 *
	 * @param promptSize
	 * @param location
	 * @param dateTime
	 */
	public PromptAnswerGroup(int promptSize, @NonNull LatLng location, @NonNull DateTime dateTime) {
		mPromptAnswers = new ArrayList<>(promptSize);
		mCapacity = promptSize;
		mLocation = location;
		mSubmitDate = dateTime;
		mPosition = 0;
		mUUID = UUID.randomUUID().toString();
	}

	/** Constructor for new prompt group for dialog
	 *
	 * @param context
	 */
	public PromptAnswerGroup(@NonNull Context context) {
		mCapacity = SharedPreferenceManager.getInstance(context).getNumberOfPrompts();
		mPromptAnswers = new ArrayList<>(mCapacity);
		mLocation = Session.getInstance().getGeofenceLatLng();
		mSubmitDate = new DateTime(Session.getInstance().getGeofenceTimestamp());
		mPosition = 0;
		mUUID = UUID.randomUUID().toString();
	}

	public String getUUID() {
		return mUUID;
	}

	public DateTime getSubmitDate() {
		return mSubmitDate;
	}

	public LatLng getLatLng() {
		return mLocation;
	}

	public double getLatitude() {
		return mLocation.latitude;
	}

	public double getLongitude() {
		return mLocation.longitude;
	}

	public List<PromptAnswer> getPromptAnswers() {
		return mPromptAnswers;
	}

	public boolean hasNext() {
		return mPosition < mCapacity;
	}

	public void setPromptAnswer(int position, PromptAnswer promptAnswer) {
		mPromptAnswers.set(position, promptAnswer);
	}

	public void setCurrentPromptAndIncrement(PromptAnswer promptAnswer) {
		promptAnswer.setUuid(mUUID);
		promptAnswer.setPromptNumber(mPosition);
		promptAnswer.setDisplayedAt(mSubmitDate.toString(ISODateTimeFormat.dateTime()));
		promptAnswer.setCancelled(false);

		if (mPosition < mPromptAnswers.size()) mPromptAnswers.set(mPosition, promptAnswer);
		else mPromptAnswers.add(promptAnswer);

		mPosition++;
	}

	public PromptAnswer getCurrentPromptAnswer() {
		if (mPosition < mPromptAnswers.size()) return mPromptAnswers.get(mPosition);

		return null;
	}

	public boolean equalsDay(PromptAnswerGroup g) {
		return (getSubmitDate().year().equals(g.getSubmitDate().year()) && getSubmitDate().dayOfYear().equals(g.getSubmitDate().dayOfYear()));
	}

	public int size() {
		return getPromptAnswers().size();
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		mPosition = position;
	}

	public static List<PromptAnswerGroup> sortPrompts(List<PromptAnswer> promptAnswers, int promptSize) {
		ArrayList<PromptAnswerGroup> group = new ArrayList<>();
		Collections.sort(promptAnswers);

		promptSize = Math.max(promptSize, 1);

		for (int i = 0; i < promptAnswers.size() / promptSize ; i++) {
			group.add(new PromptAnswerGroup(promptAnswers.subList(i * promptSize, (i + 1) * promptSize)));
		}

		return group;
	}

}
