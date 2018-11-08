package ca.itinerum.android.utilities.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ca.itinerum.android.sync.retrofit.Coordinate;
import io.reactivex.Flowable;

@Dao
public interface LocationDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(LocationPoint... points);

	@Update
	void update(LocationPoint... points);

	@Query("SELECT * FROM points")
	List<LocationPoint> getAllPoints();

	@Query("SELECT * FROM points")
	Flowable<List<LocationPoint>> getAllPointsFlowable();

	@Query("SELECT * FROM points WHERE haccuracy < :minAccuracy")
	Flowable<List<LocationPoint>> getAllPointsFlowable(int minAccuracy);

	@Query("SELECT * FROM points WHERE :minDate < timestamp AND timestamp < :maxDate")
	Flowable<List<LocationPoint>> getAllPointsBetweenDatesFlowable(String minDate, String maxDate);

	@Query("SELECT * FROM points WHERE haccuracy < :minAccuracy AND :minDate < timestamp AND timestamp < :maxDate ORDER BY timestamp ASC")
	Flowable<List<LocationPoint>> getAllPointsBetweenDatesFlowable(int minAccuracy, String minDate, String maxDate);

	@Query("SELECT * FROM points WHERE :minDate < timestamp AND timestamp < :maxDate")
	Flowable<List<Coordinate>> getAllCoordinatesBetweenDatesFlowable(String minDate, String maxDate);

	@Query("SELECT * FROM points")
	List<Coordinate> getAllCoordinates();

	@Query("SELECT * FROM points WHERE :minDate < timestamp AND timestamp < :maxDate")
	List<LocationPoint> getAllPointsBetweenDates(String minDate, String maxDate);

	@Query("SELECT * FROM points WHERE :minDate < timestamp AND timestamp < :maxDate ORDER BY timestamp ASC")
	List<Coordinate> getAllCoordinatesBetweenDates(String minDate, String maxDate);

	@Delete
	void deleteLocationPoints(LocationPoint... points);

	/** !!! be careful */
	@Query("DELETE FROM points")
	void nukeTable();

}