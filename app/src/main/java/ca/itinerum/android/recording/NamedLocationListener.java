package ca.itinerum.android.recording;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;

@SuppressWarnings("HardCodedStringLiteral")
class NamedLocationListener implements LocationListener {

	private final String mName;
	private final LocationLoggingService mService;

	NamedLocationListener(LocationLoggingService service, String name) {
		mService = service;
		mName = name;
	}

	@Override
	public void onLocationChanged(Location loc) {

		try {
			if (loc != null) {
				Bundle b = new Bundle();
				b.putString("LISTENER", mName);
				loc.setExtras(b);

				mService.OnLocationChanged(loc);
			}

		} catch (Exception ex) {
			Logger.l.e(ex.toString());
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		mService.RestartGpsManagers();
	}

	@Override
	public void onProviderEnabled(String provider) {
		mService.RestartGpsManagers();
		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(true));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			Logger.l.d("Location provider", mName, "status set to out of service");
//			mService.stopManagerAndResetAlarm();
		}

		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			Logger.l.d("Location provider", mName, "status set to temporarily unavailable");
//			mService.stopManagerAndResetAlarm();
		}
	}
}
