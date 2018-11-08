package ca.itinerum.android.utilities.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import ca.itinerum.android.sync.retrofit.PromptAnswer;

/**
 * Created by stewjacks on 2018-02-06.
 */

@Database(entities = {LocationPoint.class, PromptAnswer.class}, version = 5)
public abstract class ItinerumDatabase extends RoomDatabase {
	private static ItinerumDatabase INSTANCE;
	public abstract LocationDao locationDao();
	public abstract PromptDao promptDao();

	public static ItinerumDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (ItinerumDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
							ItinerumDatabase.class, "loc.db")
							.addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
							.allowMainThreadQueries() //TODO: implement RX and remove this line. This DB is very light so this shouldn't be too impactful
							.build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration MIGRATION_4_5 = new Migration(4, 5) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE prompts ADD COLUMN cancelled_at TEXT;");
		}
	};

	static final Migration MIGRATION_3_4 = new Migration(3, 4) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE prompts ADD COLUMN user_defined INTEGER DEFAULT 0 NOT NULL;");
		}
	};

	static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE points RENAME TO _table1_old;");
			database.execSQL("CREATE TABLE points (" +
					"_id INTEGER PRIMARY KEY NOT NULL, " +
					"altitude REAL NOT NULL, " +
					"speed REAL NOT NULL, " +
					"haccuracy REAL NOT NULL, " +
					"vaccuracy REAL NOT NULL, " +
					"longitude REAL NOT NULL, " +
					"activity INTEGER NOT NULL, " +
					"latitude REAL NOT NULL, " +
					"timestamp TEXT NOT NULL);");

			database.execSQL("INSERT INTO points (_id, altitude, speed, haccuracy, vaccuracy, longitude, activity, latitude, timestamp) " +
					"SELECT _id, altitude, speed, haccuracy, vaccuracy, longitude, activity, latitude, timestamp " +
					"FROM _table1_old;");
			database.execSQL("DROP TABLE _table1_old;");

			database.execSQL("DROP TABLE IF EXISTS prompts;");
			database.execSQL("CREATE TABLE prompts " +
					"(_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
					+ "latitude REAL NOT NULL, "
					+ "longitude REAL NOT NULL, "
					+ "displayed_at TEXT NOT NULL, "
					+ "recorded_at TEXT NOT NULL, "
					+ "prompt TEXT NOT NULL, "
					+ "answer TEXT NOT NULL, "
					+ "uuid TEXT NOT NULL, "
					+ "prompt_num INTEGER NOT NULL, "
					+ "cancelled INTEGER NOT NULL, "
					+ "uploaded INTEGER NOT NULL);");
		}
	};

}

