package ca.itinerum.android.utilities.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ca.itinerum.android.sync.retrofit.PromptAnswer;
import io.reactivex.Flowable;

@Dao
public interface PromptDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(PromptAnswer... promptAnswers);

	@Update
	void update(PromptAnswer... promptAnswers);

	@Query("SELECT * FROM prompts")
	List<PromptAnswer> getAllPromptAnswers();

	/**
	 *
	 * @return a list of PromptAnswer objects that weren't cancelled
	 */
	@Query("SELECT * FROM prompts WHERE cancelled = '0'")
	List<PromptAnswer> getAllRegisteredPromptAnswers();

	/**
	 *
	 * @param fromDate
	 * @param toDate
	 * @return a Flowable containing a list of PromptAnswer objects that weren't cancelled
	 */
	@Query("SELECT * FROM prompts WHERE :fromDate < recorded_at AND recorded_at < :toDate AND cancelled = '0'")
	Flowable<List<PromptAnswer>> getAllRegisteredPromptAnswersFlowable(String fromDate, String toDate);

	@Query("SELECT * FROM prompts WHERE :fromDate < recorded_at AND recorded_at < :toDate")
	Flowable<List<PromptAnswer>> getAllPromptAnswersFlowable(String fromDate, String toDate);
	
	@Query("SELECT * FROM prompts WHERE uploaded = '0' AND cancelled = '0'")
	List<PromptAnswer> getAllUnsyncedRegisteredPromptAnswers();

	@Query("SELECT * FROM prompts WHERE uploaded = '0' AND cancelled = '1'")
	List<PromptAnswer> getAllUnsyncedCancelledPromptAnswers();

	@Query("SELECT * FROM prompts ORDER BY _id DESC LIMIT 1")
	PromptAnswer getLastPromptAnswer();

	@Query("SELECT COUNT() FROM prompts")
	int getCount();

	@Delete
	void deletePromptAnswers(PromptAnswer... promptAnswers);

	@Query("DELETE FROM prompts")
	void nukeTable();

}
