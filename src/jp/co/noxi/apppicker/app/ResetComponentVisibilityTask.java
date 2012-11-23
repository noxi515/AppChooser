package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ActivityClassInfo;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.sqlite.ActivityDatabaseHelper;
import jp.co.noxi.apppicker.util.Common;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 指定Componentの可視性をリセットするタスク
 */
public final class ResetComponentVisibilityTask extends IntentService {

    public static void execute(Context context, String action, String mimeType) {
        final Intent intent = new Intent(context, ResetComponentVisibilityTask.class);
        intent.putExtra(Component.EXTRA_ACTION, action);
        if (mimeType != null) {
            intent.putExtra(Component.EXTRA_MIME_TYPE, mimeType);
        }
        context.startService(intent);
    }

    private static final String TAG = ResetComponentVisibilityTask.class.getSimpleName();

    public ResetComponentVisibilityTask() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onHandleIntent: argument is null");
            return;
        }

        final String action = intent.getStringExtra(Component.EXTRA_ACTION);
        final String mimeType = intent.getStringExtra(Component.EXTRA_MIME_TYPE);
        if (action == null) {
            Log.w(TAG, "Action is missing.");
            return;
        }

        final ContentValues values = new ContentValues();
        values.put(ActivityClassInfo.VISIBILITY, "visible");

        final String where;
        final String[] whereArgs;
        if (mimeType == null) {
            where = ActivityClassInfo.ACTION + "=? AND " +
                    ActivityClassInfo.MIME_TYPE + " IS NULL";
            whereArgs = new String[] {
                    action
            };
        } else {
            where = ActivityClassInfo.ACTION + "=? AND " +
                    ActivityClassInfo.MIME_TYPE + "=?";
            whereArgs = new String[] {
                    action,
                    mimeType
            };
        }

        try {
            final SQLiteDatabase db = new ActivityDatabaseHelper(this).getWritableDatabase();
            db.beginTransaction();
            try {
                final int rows = db.update(ActivityClassInfo.TABLE_ACTIVITY, values, where, whereArgs);
                if (Common.DEBUG) {
                    Log.d(TAG, rows + " rows updated");
                }
                db.setTransactionSuccessful();
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
