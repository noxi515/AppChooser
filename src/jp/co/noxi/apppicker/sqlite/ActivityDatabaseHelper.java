package jp.co.noxi.apppicker.sqlite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.co.noxi.apppicker.content.ActivityClassInfo;
import jp.co.noxi.apppicker.util.Common;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 利用したActivityの一覧を保持するデータベース
 */
public final class ActivityDatabaseHelper extends SQLiteOpenHelper {

	private static SQLiteDatabase sDatabase;

	public static synchronized SQLiteDatabase openDatabase(Context context) {
		if (sDatabase == null || !sDatabase.isOpen()) {
			sDatabase = new ActivityDatabaseHelper(
					context.getApplicationContext()).getWritableDatabase();
		}
		return sDatabase;
	}

	private static final String DB_NAME = "packagelist.sqlite3";
	private static final int DB_VERSION = 3;

	private final Context mContext;

	public ActivityDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ActivityClassInfo.TABLE_ACTIVITY + " ("
				+ ActivityClassInfo._ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ActivityClassInfo.CLASS_NAME   + " TEXT NOT NULL,"
				+ ActivityClassInfo.PACKAGE_NAME + " TEXT NOT NULL,"
				+ ActivityClassInfo.LABEL        + " TEXT NOT NULL,"
				+ ActivityClassInfo.COUNT        + " INTEGER DEFAULT 0,"
				+ ActivityClassInfo.VISIBILITY   + " TEXT NOT NULL DEFAULT 'visible',"
				+ ActivityClassInfo.ACTION       + " TEXT NOT NULL,"
				+ ActivityClassInfo.MIME_TYPE    + " TEXT,"
				+ ActivityClassInfo.LAST_TIME    + " INTEGER NOT NULL DEFAULT -1"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
		{
			final Set<Item> itemSet = new HashSet<Item>();
			Cursor cursor = null;
			try {
				cursor = db.rawQuery("SELECT * FROM packagelist", null);
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						Item item = new Item();
						final int count = cursor.getColumnCount();
						final String[] names = cursor.getColumnNames();
						for (int i = 0 ; i < count ; i++) {
							if (cursor.isNull(i)) {
								continue;
							}

							if (names[i].equals("name")) {
								item.className = cursor.getString(i);
							} else if (names[i].equals("count")) {
								item.count = cursor.getInt(i);
							} else if (names[i].equals("hide")) {
								item.hide = cursor.getInt(i) == 0;
							} else if (names[i].equals("action")) {
								item.action = cursor.getString(i);
							} else if (names[i].equals("type")) {
								item.type = cursor.getString(i);
							}
						}
						itemSet.add(item);
					}
				}
			} catch (Exception e) {
				if (Common.DEBUG) {
					e.printStackTrace();
				}
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			db.execSQL("DROP TABLE packagelist");
			db.execSQL("CREATE TABLE " + ActivityClassInfo.TABLE_ACTIVITY + " ("
					+ ActivityClassInfo._ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ ActivityClassInfo.CLASS_NAME   + " TEXT NOT NULL,"
					+ ActivityClassInfo.PACKAGE_NAME + " TEXT NOT NULL,"
					+ ActivityClassInfo.LABEL        + " TEXT NOT NULL,"
					+ ActivityClassInfo.COUNT        + " INTEGER DEFAULT 0,"
					+ ActivityClassInfo.VISIBILITY   + " TEXT NOT NULL DEFAULT 'visible',"
					+ ActivityClassInfo.ACTION       + " TEXT NOT NULL,"
					+ ActivityClassInfo.MIME_TYPE    + " TEXT,"
					+ ActivityClassInfo.LAST_TIME    + " INTEGER NOT NULL DEFAULT -1"
					+ ")");

			if (itemSet.size() > 0) {
				final PackageManager pm = mContext.getPackageManager();
				final List<PackageInfo> packageList = pm.getInstalledPackages(0);
				final Set<Item> removeItems = new HashSet<Item>();
				final ContentValues values = new ContentValues();
				packageLoop:
					for (PackageInfo pi : packageList) {
						try {
							pi = pm.getPackageInfo(pi.packageName, PackageManager.GET_ACTIVITIES);
							if (pi.activities != null) {
								for (ActivityInfo ai : pi.activities) {
									removeItems.clear();
									for (Item item : itemSet) {
										if(item.className.equals(ai.name)) {
											CharSequence label = ai.loadLabel(pm);
											if (label == null) {
												label = ai.name;
											}

											values.clear();
											values.put(ActivityClassInfo.CLASS_NAME, ai.name);
											values.put(ActivityClassInfo.PACKAGE_NAME, ai.applicationInfo.packageName);
											values.put(ActivityClassInfo.LABEL, label.toString());
											values.put(ActivityClassInfo.COUNT, item.count);
											values.put(ActivityClassInfo.VISIBILITY, item.hide ? "invisible" : "visible");
											values.put(ActivityClassInfo.ACTION, item.action);
											if (item.type != null) {
												values.put(ActivityClassInfo.MIME_TYPE, item.type);
											}
											db.insert(ActivityClassInfo.TABLE_ACTIVITY, null, values);
											removeItems.add(item);
										}
									}
									if (removeItems.size() > 0) {
										itemSet.removeAll(removeItems);
									}

									if (itemSet.size() == 0) {
										break packageLoop;
									}
								}
							}
						} catch (PackageManager.NameNotFoundException e) {
						}
					}
			}
		}
		break;

		case 2:
			db.execSQL("ALTER TABLE " + ActivityClassInfo.TABLE_ACTIVITY + " ADD COLUMN "
					+ ActivityClassInfo.VISIBILITY + " TEXT NOT NULL DEFAULT 'visible';");
			db.execSQL("ALTER TABLE " + ActivityClassInfo.TABLE_ACTIVITY + " ADD COLUMN "
					+ ActivityClassInfo.LAST_TIME + " INTEGER NOT NULL DEFAULT -1;");
			db.execSQL("UPDATE " + ActivityClassInfo.TABLE_ACTIVITY + " SET "
					+ ActivityClassInfo.VISIBILITY + "=CASE WHEN hide=1 THEN 'visible' ELSE 'invisible' END;");
		}
	}

	static class Item {
		String className;
		int count;
		boolean hide;
		String action;
		String type;
	}
}