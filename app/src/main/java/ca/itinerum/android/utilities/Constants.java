package ca.itinerum.android.utilities;

@SuppressWarnings("HardCodedStringLiteral")
public class Constants
{

	/* Data Points Recording Constants */
    public final static int SMALLEST_DISPLACEMENT = 25; // in meters 100 / 200
	public static final int LARGEST_VELOCITY = 150; // 150 m/s = 540km/h
    public final static int RETRY_INTERVAL = 60;
	public final static int RECORDING_INTERVAL = 10;
	/* The absolute timeout for a GPS point to be received in seconds */
	public static final int ABSOLUTE_TIMEOUT = 120;

	// other point types

	public static float TARGET_ACCURACY = 30f;
}
