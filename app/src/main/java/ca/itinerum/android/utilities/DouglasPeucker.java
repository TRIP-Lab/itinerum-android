package ca.itinerum.android.utilities;

import java.util.ArrayList;
import java.util.List;

import ca.itinerum.android.utilities.db.LocationPoint;

/**
 * Created by stewjacks on 2018-02-12.
 */

public class DouglasPeucker {

	private DouglasPeucker() {}

	public static List<LocationPoint> filter(List<LocationPoint> points, double epsilon)
	{
		// Find the point with the maximum distance
		double dmax = 0;
		int index = 0;
		for (int i = 1; i < points.size() - 1; i++)
		{
			double d =  points.get(i).getPerpendicularDistanceFromLine(points.get(0), points.get(points.size()-1));
			if (d > dmax)
			{
				index = i;
				dmax = d;
			}
		}

		List<LocationPoint> resultList = null;

		if(points.size() < 2)
		{
			return points;
		}
		else if (dmax >= epsilon)       // If max distance is greater than epsilon, recursively simplify
		{
			// Recursive call
			List<LocationPoint> recResults1 = filter(points.subList(0, index), epsilon);
			List<LocationPoint> recResults2 = filter(points.subList(index, points.size()), epsilon);

			// Build the result list
			resultList = new ArrayList<>(recResults1.size() + recResults2.size()) ;
			resultList.addAll(recResults1);
			resultList.addAll(recResults2);
		}
		else
		{
			resultList = new ArrayList<>(2);
			resultList.add(0, points.get(0));
			resultList.add(1, points.get(points.size()-1));
		}

		return resultList;
	}

}
