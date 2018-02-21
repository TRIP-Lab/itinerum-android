package ca.itinerum.android.utilities.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.Logger;

@SuppressWarnings("HardCodedStringLiteral")
public class ModePromptHelper extends SQLiteOpenHelper {
	public static final String TABLE_MODEPROMPT = "modeprompt"; //changing to prompt
	public static final String COLUMN_ID = "_id";
	public static final String UPLOADED = "uploaded";
	public static final String ANSWER = "answer";
	public static final String PROMPT = "prompt";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String RECORDED_AT = "recorded_at";
	public static final String TIMESTAMP = "timestamp";

	public static final String DATABASE_NAME = "modeprompt.db";
	private static final int DATABASE_VERSION = 3;

	private String[] allColumns = { COLUMN_ID, LATITUDE, LONGITUDE, TIMESTAMP, RECORDED_AT, PROMPT, ANSWER, UPLOADED};

	private static final String DATABASE_CREATE = "create table "
		+ TABLE_MODEPROMPT + "(" + COLUMN_ID + " integer primary key autoincrement, "
		+ LATITUDE + " REAL not null, "
		+ LONGITUDE + " REAL not null, "
		+ TIMESTAMP + " TEXT not null, "
		+ RECORDED_AT + " TEXT not null, "
		+ PROMPT + " TEXT not null, "
		+ ANSWER + " TEXT not null, "
		+ UPLOADED + " INTEGER not null "
		  + ");";
	private static ModePromptHelper sInstance;

	public static synchronized ModePromptHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new ModePromptHelper(context);
		}
		return sInstance;
	}

	public ModePromptHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);

	}

	public List<PromptAnswer> getAllMigrationPromptAnswers() {
		List<PromptAnswer> modePromptList = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(TABLE_MODEPROMPT, allColumns, "prompt != ?", new String[]{"FIRST_STOP"}, null, null, null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				PromptAnswer modePromptObject = cursorToPromptAnswer(cursor);
				modePromptList.add(modePromptObject);
				cursor.moveToNext();
			}
		} finally {
			// make sure to close the cursor
			cursor.close();
		}

		return modePromptList;
	}

	private PromptAnswer cursorToPromptAnswer(Cursor cursor) {

		//This will generate a UUID as it doesn't exist in the old table
		PromptAnswer modePromptObject = new PromptAnswer()
				.withLatitude(cursor.getDouble(1))
				.withLongitude(cursor.getDouble(2))
				.withDisplayedAt(cursor.getString(3))
				.withRecordedAt(cursor.getString(4))
				.withPrompt(cursor.getString(5))
				.withJsonAnswer(cursor.getString(6))
				.withUploaded(cursor.getInt(7) == 1);

		modePromptObject.setId(cursor.getLong(0));

		return modePromptObject;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.l.w(ModePromptHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODEPROMPT);
	    onCreate(db);
	}

}
