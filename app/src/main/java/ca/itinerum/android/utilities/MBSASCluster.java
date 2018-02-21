package ca.itinerum.android.utilities;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.util.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by stewjacks on 2016-04-09.
 */
public class MBSASCluster<T extends Clusterable> extends Clusterer<T> {

	/**
	 * Build a new clusterer with the given {@link DistanceMeasure}.
	 *
	 * @param measure the distance measure to use
	 */

	private final double eps;

	public MBSASCluster(final double eps) throws NotPositiveException {
		this(eps, new EuclideanDistance());
	}

	/**
	 * Creates a new instance of a DBSCANClusterer.
	 *
	 * @param eps maximum radius of the neighborhood to be considered
	 * @param measure the distance measure to use
	 * @throws NotPositiveException if {@code eps < 0.0} or {@code minPts < 0}
	 */
	public MBSASCluster(final double eps, final DistanceMeasure measure) throws NotPositiveException {
		super(measure);

		if (eps < 0.0d) {
			throw new NotPositiveException(eps);
		}

		this.eps = eps;
	}

	@Override
	public List<CentroidCluster<T>> cluster(final Collection<T> points) throws NullArgumentException {

		// sanity checks
		MathUtils.checkNotNull(points);

		final List<T> ptList = new ArrayList<>(points);
		final List<CentroidCluster<T>> clusters = new ArrayList<>();

		if (ptList.size() == 0) return clusters;

		// initialize the first cluster
		CentroidCluster<T> c = new CentroidCluster<>(ptList.get(0));
		c.addPoint(ptList.get(0));

		CentroidCluster<T> currentCluster = c;

		// iterate over all points and assign cluster
		for (int i = 1; i < ptList.size(); i++) {
			T currentPoint = ptList.get(i);
			double distance = distance(currentCluster.getCenter(), currentPoint);
			if (distance > eps) {
				// add the last cluster
				clusters.add(currentCluster);
				currentCluster = new CentroidCluster<>(currentPoint);
				currentCluster.addPoint(currentPoint);
			} else {
				// add it to the cluster
				currentCluster.addPoint(currentPoint);
				currentCluster = updateClusterCenter(currentCluster);
			}
		}

		clusters.add(currentCluster);

		return clusters;
	}

	private List<T> getNeighbors(final T point, final Collection<T> points) {
		final List<T> neighbors = new ArrayList<>();
		for (final T neighbor : points) {
			if (point != neighbor && distance(neighbor, point) <= eps) {
				neighbors.add(neighbor);
			}
		}
		return neighbors;
	}

	private CentroidCluster<T> updateClusterCenter(CentroidCluster<T> cluster) {
		double[] centre = cluster.getPoints().get(0).getPoint();
		double[] centroidSum = centre;
		for (int i = 1; i < cluster.getPoints().size(); i++) {
			double[] newPoint = cluster.getPoints().get(i).getPoint();
			for (int j = 0; j < centre.length; j++) {
				centroidSum[j] += newPoint[j];
			}
		}

		for (int i = 0; i < centroidSum.length; i++) {
			centre[i] = centroidSum[i]/cluster.getPoints().size();
		}

		CentroidCluster<T> newCluster = new CentroidCluster<>(new DoublePoint(centre));
		for (final T point: cluster.getPoints()) {
			newCluster.addPoint(point);
		}

		return newCluster;

	}
}
