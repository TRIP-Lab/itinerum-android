package ca.itinerum.android.utilities;

public abstract class LocationLoggingEvent {
	/**
	 * Requests starting or stopping the logging service.
	 * Called from the fragment button click events
	 */
	public static class PauseResume {
		public boolean pause;
		public PauseResume(boolean pause) { this.pause = pause; }
	}

	/**
	 * Requests toggling of location logging service
	 */
	public static class StartStop {
		public boolean start;
		public StartStop(boolean start){
			this.start = start;
		}
	}
	
	/**
	 * Triggered at a defined interval after a geofence is created
	 */
	public static class PromptGeofenceLoiter {}

	/**
	 * Triggered at a defined interval after a geofence has been loitered in
	 */
	public static class PromptGeofenceDwell {}

	/**
	 * This will trigger a message to the user that they have dwelled, assuming the activity is open.
	 */
	public static class ShowGeofencePromptInActivity {}

	public static class PromptImmediate {}
}

