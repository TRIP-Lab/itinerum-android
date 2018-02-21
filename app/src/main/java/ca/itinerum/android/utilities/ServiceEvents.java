/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

/* modified on 20-02-2018 by stewjacks */

package ca.itinerum.android.utilities;

public class ServiceEvents {

	/**
	 * Status message
	 */
	public static class StatusMessage {
		public String status;

		public StatusMessage(String message) {
			this.status = message;
		}

	}

	/**
	 * User feedback message for events
	 */
	public static class UserMessage {
		public int message;

		public UserMessage(int message) {
			this.message = message;
		}

	}

	/**
	 * Error message
	 */
	public static class FatalMessage {
		public String message;

		public FatalMessage(String message) {
			this.message = message;
		}
	}

	/**
	 * Indicates that GPS/Network location services have temporarily gone away
	 */
	public static class LocationServicesUnavailable {
	}

	/**
	 * Whether GPS logging has started; raised after the start/stop button is pressed
	 */
	public static class LoggingStatus {
		public boolean loggingStarted;

		public LoggingStatus(boolean loggingStarted) {
			this.loggingStarted = loggingStarted;
		}
	}
}
