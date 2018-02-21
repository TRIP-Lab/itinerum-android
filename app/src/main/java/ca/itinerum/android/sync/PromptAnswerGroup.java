package ca.itinerum.android.sync;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.itinerum.android.sync.retrofit.PromptAnswer;

/**
 * Created by stewjacks on 2018-01-30.
 */

public class PromptAnswerGroup {

	private List<PromptAnswer> mPromptAnswers;

	private int mPosition;
	private final LatLng mLocation;
	private final DateTime mSubmitDate;


	// Constructor for existing prompt group
	public PromptAnswerGroup(List<PromptAnswer> promptAnswers) {
		mPromptAnswers = promptAnswers;
		mLocation = new LatLng(promptAnswers.get(0).getLatitude(), promptAnswers.get(0).getLongitude());
		mSubmitDate = DateTime.parse(mPromptAnswers.get(0).getRecordedAt());
	}

	// Constructor for new prompt group
	public PromptAnswerGroup(int promptSize, LatLng location, DateTime dateTime) {
		mPromptAnswers = new ArrayList<>(promptSize);
		mLocation = location;
		mSubmitDate = dateTime;
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

		for (int i = 0; i < promptAnswers.size() / promptSize ; i++) {
			group.add(new PromptAnswerGroup(promptAnswers.subList(i * promptSize, (i + 1) * promptSize)));
		}

		return group;
	}

}
