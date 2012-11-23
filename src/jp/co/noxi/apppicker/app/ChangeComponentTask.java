package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ActivityClassInfo;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.sqlite.ActivityDatabaseHelper;
import jp.co.noxi.apppicker.util.Common;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

abstract class ChangeComponentTask extends IntentService {

    private static final String TAG = ChangeComponentTask.class.getSimpleName();

    public ChangeComponentTask(String name) {
        super(name);
    }

    /**
     * 
     * @param intent
     * @param values
     */
    public final void onHandleIntent(Intent intent, ContentValues values) {
        final String action = intent.getStringExtra(Component.EXTRA_ACTION);
        final String packageName = intent.getStringExtra(Component.EXTRA_PACKAGE_NAME);
        final String className = intent.getStringExtra(Component.EXTRA_CLASS_NAME);
        final String mimeType = intent.getStringExtra(Component.EXTRA_MIME_TYPE);
        final String label = intent.getStringExtra(Component.EXTRA_LABEL);

        if (action == null) {
            Log.e(TAG, "Action is missing.");
            return;
        }
        if (packageName == null) {
            Log.e(TAG, "PackageName is missing.");
            return;
        }
        if (className == null) {
            Log.e(TAG, "ClassName is missing.");
            return;
        }
        if (label == null) {
            Log.e(TAG, "Component label is missing.");
            return;
        }

        final String where;
        final String[] whereArgs;
        if (mimeType == null) {
            where = ActivityClassInfo.CLASS_NAME + "=? AND " +
                    ActivityClassInfo.PACKAGE_NAME + "=? AND " +
                    ActivityClassInfo.ACTION + "=? AND " +
                    ActivityClassInfo.MIME_TYPE + " IS NULL";
            whereArgs = new String[] {
                    className,
                    packageName,
                    action
            };
        } else {
            where = ActivityClassInfo.CLASS_NAME + "=? AND " +
                    ActivityClassInfo.PACKAGE_NAME + "=? AND " +
                    ActivityClassInfo.ACTION + "=? AND " +
                    ActivityClassInfo.MIME_TYPE + "=?";
            whereArgs = new String[] {
                    className,
                    packageName,
                    action,
                    mimeType
            };
        }
        try {
            final SQLiteDatabase db = new ActivityDatabaseHelper(this).getWritableDatabase();
            try {
                db.beginTransaction();
                final int rows = db.update(ActivityClassInfo.TABLE_ACTIVITY, values, where, whereArgs);
                if (rows == 0) {
                    values.put(ActivityClassInfo.ACTION, action);
                    values.put(ActivityClassInfo.PACKAGE_NAME, packageName);
                    values.put(ActivityClassInfo.CLASS_NAME, className);
                    values.put(ActivityClassInfo.LABEL, label);
                    if (mimeType != null) {
                        values.put(ActivityClassInfo.MIME_TYPE, mimeType);
                    }
                    db.insertOrThrow(ActivityClassInfo.TABLE_ACTIVITY, null, values);
                }
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                if (Common.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                db.endTransaction();
            }
        } catch (SQLException e) {
            if (Common.DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
