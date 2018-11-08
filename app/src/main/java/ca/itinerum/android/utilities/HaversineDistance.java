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
		return r[0];
	}
}
