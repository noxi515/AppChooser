package jp.co.noxi.apppicker.content;

import java.util.Date;
import java.util.List;

import jp.co.noxi.apppicker.sqlite.ActivityDatabaseHelper;
import jp.co.noxi.apppicker.util.Common;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * コンポーネント一覧を読み込むローダー
 */
public class ComponentLoader extends AsyncTaskLoader<ComponentHolder> {

    private static final String TAG = ComponentLoader.class.getSimpleName();

    final Intent mIntent;
    final Intent[] mInitialIntent;
    final PackageManager mPackageManager;

    ComponentHolder mComponents;

    public ComponentLoader(Context context, Intent intent, Intent[] initialIntents) {
        super(context);
        mIntent = intent;
        mInitialIntent = initialIntents;
        mPackageManager = context.getPackageManager();
    }

    /**
     * This is where the bulk of our work is done. This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public ComponentHolder loadInBackground() {
        if (Common.DEBUG) {
            Log.d(TAG, "AppChooser loadInBackground start");
        }
        List<ResolveInfo> resolves = mPackageManager
                .queryIntentActivities(mIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (mInitialIntent != null) {
            for (Intent ii : mInitialIntent) {
                if (ii == null) {
                    continue;
                }
                final ActivityInfo ai = ii.resolveActivityInfo(mPackageManager, 0);
                if (ai == null) {
                    Log.w(TAG, "No activity found for " + ii);
                    continue;
                }
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                if (ii instanceof LabeledIntent) {
                    final LabeledIntent li = (LabeledIntent) ii;
                    ri.resolvePackageName = li.getSourcePackage();
                    ri.labelRes = li.getLabelResource();
                    ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                    ri.icon = li.getIconResource();
                }
                resolves.add(ri);
            }
        }

        final int size = resolves.size();
        mComponents = new ComponentHolder(size);
        if (size > 0) {
            final String action = mIntent.getAction();
            final String mimeType = mIntent.getType();
            final String[] columns = {
                    ActivityClassInfo.CLASS_NAME,
                    ActivityClassInfo.PACKAGE_NAME,
                    ActivityClassInfo.COUNT,
                    ActivityClassInfo.VISIBILITY,
                    ActivityClassInfo.LAST_TIME
            };
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

            final boolean[] foundArray = new boolean[size];
            final int[] hashArray = new int[size];
            final String sql = SQLiteQueryBuilder.buildQueryString(false,
                    ActivityClassInfo.TABLE_ACTIVITY, columns, where, null, null, null, null);
            Cursor cursor = null;
            try {
                if (Common.DEBUG) {
                    Log.d(TAG, "AppChooser loadInBackground db_access start");
                }

                final SQLiteDatabase db = new ActivityDatabaseHelper(getContext()).getReadableDatabase();
                cursor = db.rawQuery(sql, whereArgs);

                ResolveInfo ri = null;
                while (cursor.moveToNext()) {
                    final int classNameHash = cursor.getString(0).hashCode();
                    final int packageNameHash = cursor.getString(1).hashCode();
                    for (int i = 0; i < resolves.size(); i++) {
                        if (hashArray[i] == 0) {
                            ri = resolves.get(i);
                            hashArray[i] = ri.activityInfo.packageName.hashCode();
                        }
                        if (hashArray[i] == packageNameHash) {
                            if (ri == null) {
                                ri = resolves.get(i);
                            }
                            if (classNameHash == ri.activityInfo.name.hashCode()) {
                                final Component component = new Component(mPackageManager, ri,
                                        cursor.getInt(2), cursor.getString(3).charAt(0) == 'v');
                                component.lastUseTime = new Date(cursor.getLong(4));
                                mComponents.add(component);
                                foundArray[i] = true;
                                ri = null;
                                break;
                            }
                        }

                        ri = null;
                    }
                }
            } catch (SQLException e) {
                if (Common.DEBUG) {
                    e.printStackTrace();
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

            ResolveInfo ri;
            final int myPackageHash = getContext().getPackageName().hashCode();
            for (int i = 0; i < size; i++) {
                if (!foundArray[i]) {
                    ri = resolves.get(i);
                    int hash = hashArray[i];
                    if (hash == 0) {
                        hash = ri.activityInfo.packageName.hashCode();
                    }
                    if (myPackageHash != hash) {
                        mComponents.add(new Component(mPackageManager, ri));
                    }
                }
            }
        }

        if (Common.DEBUG) {
            Log.d(TAG, "AppChooser loadInBackground end");
        }

        return mComponents;
    }

    /**
     * Called when there is new data to deliver to the client. The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(ComponentHolder components) {
        if (isReset()) {
            // An async query came in while the loader is stopped. We
            // don't need the result.
            if (components != null) {
                onReleaseResources(components);
            }
        }

        ComponentHolder oldComplnents = components;
        mComponents = components;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(components);
        }

        // At this point we can release the resources associated with
        // 'oldComponents' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldComplnents != null) {
            onReleaseResources(oldComplnents);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mComponents != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mComponents);
        } else {
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(ComponentHolder components) {
        super.onCanceled(components);

        // At this point we can release the resources associated with
        // 'components'
        // if needed.
        onReleaseResources(components);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mComponents != null) {
            onReleaseResources(mComponents);
            mComponents = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(ComponentHolder components) {
        // Do nothing
    }

}
