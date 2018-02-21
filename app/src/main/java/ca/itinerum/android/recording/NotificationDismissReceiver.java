package ca.itinerum.android.recording;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.UUID;

import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.LocationDatabase;

@SuppressWarnings("HardCodedStringLiteral")
public class NotificationDismissReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int id = intent.getIntExtra("notificationId", 0);
		if (id != 0) ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
		Session.getInstance().setGeofenceState(Session.GeofenceState.DWELL_NO_PROMPT);

		// Now insert a cancelled prompt answer
		// cancelled prompts should exist above the maximum number of prompts to avoid collision.
		int count = LocationDatabase.getInstance(context).promptDao().getCount() + SharedPreferenceManager.getInstance(context).getMaximumNumberOfPrompts() + 1;

		PromptAnswer promptAnswer = new PromptAnswer()
				.withPrompt("")
				.withAnswer("")
				.withLatitude(Session.getInstance().getLastRecordedLatitude())
				.withLongitude(Session.getInstance().getLastRecordedLongitude())
				.withDisplayedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withRecordedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withUuid(UUID.randomUUID().toString())
				.withUploaded(false)
				.withCancelled(true)
				.withPromptNumber(count);

		LocationDatabase.getInstance(context).promptDao().insert(promptAnswer);

	}
}
