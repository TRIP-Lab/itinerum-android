package ca.itinerum.android.utilities;

import android.location.Location;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

/**
 * Created by stewjacks on 16-01-08.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class HaversineDistance implements DistanceMeasure {
	@Override
	public double compute(double[] a, double[] b) {
		float[] r = new float[3];
		Location.distanceBetween(a[0], a[1], b[0], b[1], r);
		Logger.l.d("point 1", a[0], a[1], "point 2", b[0], b[1], "distance", r[0], r[1], r[2]);
		return r[0];
	}
}
