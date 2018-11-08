/*
 * Session singleton contains settings pertaining to the running instance of Itinerum
 *
 *    This is influenced by the structure of GPSLogger project: https://github.com/mendhak/gpslogger/
 */

package ca.itinerum.android.recording;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

@SuppressWarnings("HardCodedStringLiteral")
public class Session {

    private boolean sTowerEnabled;
    private boolean sGpsEnabled;
    private boolean sStarted = false;
    private boolean sIsUsingGps;
    private Location sLastValidLocation; //TODO: put this is SP
	private Location sLastRecodedLocation; //TODO: put this is SP
    private String sDescription = "";
    private boolean sIsSinglePointMode = false;
    private boolean sWaitingForLocation;
	private boolean sGeofenceDwell;
	private boolean sGeofenceActive;
    private LatLng sGeofenceLatLng = null; //TODO: put this is SP
    private long sUserStillSinceTimeStamp;
    private boolean sGeofenceLoitering;
	private int sCurrentDetectedActivity = 4; //unknown
    private boolean sGeofencePurposeRecorded;
    private boolean sShowDialog;
    private boolean sPaused = false;

    private long sGeofenceTimestamp = 0;

    public enum GeofenceState {
        NONE, ACTIVE, LOITER, DWELL, ANSWERED
	}

	private static final Session SESSION = new Session();

	private Session() {}

	public static Session getInstance() {
		return SESSION;
	}

    /**
     * Get sGeofencePurposeRecorded
     */

    public synchronized boolean isGeofencePurposeRecorded() {
        return sGeofencePurposeRecorded;
    }

    /**
     * Set sGeofencePurposeRecorded
     *
     * @param geofencePurposeRecorded
     */

    public synchronized void setGeofencePurposeRecorded(boolean geofencePurposeRecorded) {
        sGeofencePurposeRecorded = geofencePurposeRecorded;
    }

    public synchronized void setGeofenceState(GeofenceState state) {

		switch(state) {
			case NONE:
				setIsGeofenceActive(false);
				setGeofenceLoitering(false);
				setGeofenceDwell(false);
				setShowDwellDialog(false);
				setGeofencePurposeRecorded(false);
				break;
			case ACTIVE:
				setIsGeofenceActive(true);
				setGeofenceLoitering(false);
				setGeofenceDwell(false);
				setShowDwellDialog(false);
				setGeofencePurposeRecorded(false);
				break;
			case LOITER:
				setIsGeofenceActive(true);
				setGeofenceLoitering(true);
				setGeofenceDwell(false);
				setShowDwellDialog(false);
				setGeofencePurposeRecorded(false);
				break;
			case DWELL:
				setIsGeofenceActive(true);
				setGeofenceLoitering(true);
				setGeofenceDwell(true);
				setShowDwellDialog(true);
				setGeofencePurposeRecorded(false);
				break;
			case ANSWERED:
				setIsGeofenceActive(true);
				setGeofenceLoitering(true);
				setGeofenceDwell(true);
				setShowDwellDialog(false);
				setGeofencePurposeRecorded(true);
				break;
		}
    }

	public synchronized boolean isGeofenceDwell() {
		return sGeofenceDwell;
	}

	public synchronized void setGeofenceDwell(boolean dwell) {
		sGeofenceDwell = dwell;
	}

	public synchronized boolean isGeofenceLoitering() {
		return sGeofenceLoitering;
	}

	public synchronized void setGeofenceLoitering(boolean loitering) {
        sGeofenceLoitering = loitering;
    }

	public synchronized boolean isGeofenceActive() {
		return sGeofenceActive;
	}

	public synchronized void setIsGeofenceActive(boolean geofence) {
		sGeofenceActive = geofence;
	}

    public synchronized void setGeofenceLatLng(LatLng geofenceLatLng) {
        sGeofenceLatLng = geofenceLatLng;
    }

    public synchronized boolean shouldShowDwellDialog() {
        return sShowDialog;
    }

    public synchronized void setShowDwellDialog(boolean showDialog) {
        sShowDialog = showDialog;
    }

    public synchronized LatLng getGeofenceLatLng() {
        return sGeofenceLatLng;
    }

    /**
     * @return whether logging has started
     */
    public synchronized boolean isRecording() {
        return sStarted;
    }

    /**
     * @param isStarted set whether logging has started
     */
    public synchronized void setRecording(boolean isStarted) {
        Session.getInstance().sStarted = isStarted;
    }

    /**
     * @return the sIsUsingGps
     */
    public synchronized boolean isUsingGps() {
        return sIsUsingGps;
    }

    /**
     * @param isUsingGps the sIsUsingGps to set
     */
    public synchronized void setUsingGps(boolean isUsingGps) {
        Session.getInstance().sIsUsingGps = isUsingGps;
    }

	public synchronized void setCurrentDetectedActivity(int activity) {
		Session.getInstance().sCurrentDetectedActivity = activity;
	}

    /**
     * @return the current Latitude
     */
    public synchronized double getLastRecordedLatitude() {
        if (getLastRecordedLocation() != null) {
            return getLastRecordedLocation().getLatitude();
        } else {
            return 0;
        }
    }

	/**
	 * @return the current Longitude
	 */
	public synchronized double getLastRecordedLongitude() {
		if (getLastRecordedLocation() != null) {
			return getLastRecordedLocation().getLongitude();
		} else {
			return 0;
		}
	}

    /**
     * @return the latestTimestamp (for location info)
     */
    public synchronized long getLastRecordedTimestamp() {
        if (getLastRecordedLocation() != null) {
            return getLastRecordedLocation().getTime();
        } else {
            return 0;
        }
    }

	public synchronized int getCurrentDetectedActivity() {
		return Session.getInstance().sCurrentDetectedActivity;
	}

	public synchronized long getGeofenceTimestamp() {
        return sGeofenceTimestamp;
    }

    public synchronized void setGeofenceTimestamp(long geofenceTimestamp) {
        sGeofenceTimestamp = geofenceTimestamp;
    }

    /**
     * @param lastValidLocation the latest valid Location (not necessarily recorded)
     */
    public synchronized void setLastValidLocation(Location lastValidLocation) {
        Session.getInstance().sLastValidLocation = lastValidLocation;
    }

    /**
     * @return the Location class containing latest valid Location (not necessarily recorded)
     */
    public Location getLastValidLocation() {
        return sLastValidLocation;
    }

	/**
	 * @param lastRecodedLocation the latest valid and recoded Location
	 */
	public synchronized void setLastRecordedLocation(Location lastRecodedLocation) {
		Session.getInstance().sLastRecodedLocation = lastRecodedLocation;
	}

	public synchronized Location getLastRecordedLocation() {
		return sLastRecodedLocation;
	}

    public synchronized boolean hasDescription() {
        return !(sDescription.length() == 0);
    }

    public synchronized void setWaitingForLocation(boolean waitingForLocation) {
        Session.getInstance().sWaitingForLocation = waitingForLocation;
    }

    public synchronized boolean isWaitingForLocation() {
        return sWaitingForLocation;
    }

    public synchronized long getUserStillSinceTimeStamp() {
        return sUserStillSinceTimeStamp;
    }

    public synchronized void setUserStillSinceTimeStamp(long lastUserStillTimeStamp) {
        Session.getInstance().sUserStillSinceTimeStamp = lastUserStillTimeStamp;
    }

    public synchronized boolean isPaused() {
        return sPaused;
    }

    public synchronized void setPaused(boolean paused) {
        sPaused = paused;
    }

}
