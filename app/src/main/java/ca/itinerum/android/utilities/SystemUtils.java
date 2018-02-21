package ca.itinerum.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;
import java.util.UUID;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.utilities.db.LocationDatabase;

/**
 * Created by stewjacks on 2017-07-26.
 */

public class SystemUtils {

	private SystemUtils(){}

	public static String getPhoneDetails() {
		StringBuilder builder = new StringBuilder();
		builder.append(" \n\n\n")
				.append("Itinerum: ")
				.append(BuildConfig.VERSION_NAME)
				.append(" ")
				.append(BuildConfig.VERSION_CODE)
				.append("\n")
				.append(getSDKVersionName())
				.append("\n")
				.append(Build.BRAND)
				.append(" ")
				.append(Build.MODEL);

		return builder.toString();


	}

	private static String getSDKVersionName() {
		StringBuilder builder = new StringBuilder();
		builder.append("Android: ").append(Build.VERSION.RELEASE);

		Field[] fields = Build.VERSION_CODES.class.getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			int fieldValue = -1;

			try {
				fieldValue = field.getInt(new Object());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			if (fieldValue == Build.VERSION.SDK_INT) {
				builder.append(" : ").append(fieldName).append(" : ");
				builder.append("sdk=").append(fieldValue);
			}
		}

		return builder.toString();

	}

	public static Bitmap ColourBitmap(Context context, int res, int color) {
		Bitmap sourceBitmap = BitmapFactory.decodeResource(context.getResources(), res).copy(Bitmap.Config.ARGB_8888, true);
		ColorFilter filter = new PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN);

		Paint paint = new Paint();
		paint.setColorFilter(filter);

		Canvas canvas = new Canvas(sourceBitmap);
		canvas.drawBitmap(sourceBitmap, 0, 0, paint);

		return sourceBitmap;
	}

	public static void leaveCurrentSurvey(Context context) {

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(context);
		sp.deleteAllSettings();

		try {
			LocationDatabase.getInstance(context).locationDao().nukeTable();
			LocationDatabase.getInstance(context).promptDao().nukeTable();
		} catch (Exception e) {
			Logger.l.e("error clearing points db", e.toString());
		}


		// Configure a new UUID identifier
		String uuid = UUID.randomUUID().toString();
		sp.setUUID(uuid);
	}

	public static void hideKeyboardFrom(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

}
