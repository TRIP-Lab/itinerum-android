package ca.itinerum.android.utilities;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by stewjacks on 2017-01-27.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DateUtils {

	private DateUtils(){}

	public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZ";

	public static String getCurrentFormattedTime() {
		return formatDateForBackend(System.currentTimeMillis());
	}

	public static String formatDateForBackend(Date date) {
		return formatDateForBackend(new DateTime(date));
	}

	public static String formatDateForBackend(DateTime date) {
		return date.toString(ISODateTimeFormat.dateTime());
	}

	public static String formatDateForBackend(long timestamp) {
		return formatDateForBackend(new DateTime(timestamp));
	}
}
