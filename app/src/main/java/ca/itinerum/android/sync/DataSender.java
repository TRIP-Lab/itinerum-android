package ca.itinerum.android.sync;

import android.content.Context;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.itinerum.android.sync.retrofit.Coordinate;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.sync.retrofit.Update;
import ca.itinerum.android.sync.retrofit.UpdateResponse;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.ItinerumDatabase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class is in charge of sending the data points and user information
 * to the remote server.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class DataSender
{

    private Context mContext;

	public DataSender(Context context) {
        mContext = context;
    }
    
    /**
     * Sends the data to the remote server.
     *
     */
    public void sync() {
		syncChunk(SharedPreferenceManager.getInstance(mContext).getLastSyncDate(), new DateTime());
    }

    private void syncChunk(DateTime fromDate, DateTime toDate) {

        // Adding Point Update values to request

		List<Coordinate> points = ItinerumDatabase.getInstance(mContext).locationDao().getAllCoordinatesBetweenDates(DateUtils.formatDateForBackend(fromDate), DateUtils.formatDateForBackend(toDate));

		final List<PromptAnswer> prompts = ItinerumDatabase.getInstance(mContext).promptDao().getAllUnsyncedRegisteredPromptAnswers();
		final List<PromptAnswer> cancelledPrompts = ItinerumDatabase.getInstance(mContext).promptDao().getAllUnsyncedCancelledPromptAnswers();

		Update update = new Update();
		update.setUuid(SharedPreferenceManager.getInstance(mContext).getUUID());
		update.setCoordinates(points);
		update.setPrompts(prompts);
		update.setCancelledPrompts(cancelledPrompts);

		Triplab triplab = new Triplab();
		Call<UpdateResponse> call = triplab.getApi().updateReport(update);
		call.enqueue(new Callback<UpdateResponse>() {
			@Override
			public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
				Logger.l.d("onResponse", response.toString());
				if (!response.isSuccessful()) {
					if (response.errorBody() != null) Logger.l.e(response.errorBody().toString());
					// upload failed. Do not upload last sync time.
					return;
				}

				SharedPreferenceManager.getInstance(mContext).setLastSyncDate(DateUtils.formatDateForBackend(new DateTime()));

				// update prompts and cancelledPrompts that have been synced
				List<PromptAnswer> allPrompts = new ArrayList<>(prompts);
				allPrompts.addAll(cancelledPrompts);

				if (allPrompts.size() > 0) {
					for (PromptAnswer promptAnswer : allPrompts) {
						promptAnswer.setUploaded(true);
					}

					PromptAnswer[] p = new PromptAnswer[allPrompts.size()];
					p = allPrompts.toArray(p);
					ItinerumDatabase.getInstance(mContext).promptDao().update(p);
				}
			}

			@Override
			public void onFailure(Call<UpdateResponse> call, Throwable t) {
				Logger.l.e("onFailure", t.toString());
			}
		});

	}
}
