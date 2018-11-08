package ca.itinerum.android.utilities;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("HardCodedStringLiteral")
public class Constants
{

	/* Data Points Recording Constants */
    public final static int SMALLEST_DISPLACEMENT = 5; // in meters 100 / 200
	public final static int LARGEST_DISPLACEMENT = 200; // in meters 100 / 200
	public static final int LARGEST_VELOCITY = 75; // 75 m/s = 270km/h
    public final static long RETRY_INTERVAL = TimeUnit.MINUTES.toMillis(1);
//	public final static long RECORDING_INTERVAL = TimeUnit.SECONDS.toMillis(10);

	// other point types

	public static float TARGET_ACCURACY = 35f;
}
