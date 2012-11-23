package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ActivityClassInfo;
import jp.co.noxi.apppicker.content.Component;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class ChangeComponentCountTask extends ChangeComponentTask {

    public static void execute(Context context,
            Component component, String action, String mimeType) {
        if (component.packageName == null || component.className == null) {
            return;
        }

        final Intent intent = new Intent(context, ChangeComponentCountTask.class);
        intent.putExtra(Component.EXTRA_ACTION, action);
        intent.putExtra(Component.EXTRA_PACKAGE_NAME, component.packageName);
        intent.putExtra(Component.EXTRA_CLASS_NAME, component.className);
        intent.putExtra(Component.EXTRA_LABEL, component.label);
        intent.putExtra(Component.EXTRA_LAUNCH_COUNT, component.count);
        intent.putExtra(Component.EXTRA_VISIBILITY, component.visibility);
        if (mimeType != null) {
            intent.putExtra(Component.EXTRA_MIME_TYPE, mimeType);
        }
        context.startService(intent);
    }

    private static final String TAG = ChangeComponentCountTask.class.getSimpleName();
    private static final int DEFAULT_LAUNCH_COUNT = 0;

    public ChangeComponentCountTask() {
        super(TAG);
    }

    public ChangeComponentCountTask(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final int count = intent.getIntExtra(Component.EXTRA_LAUNCH_COUNT, DEFAULT_LAUNCH_COUNT);
        final boolean visibility = intent.getBooleanExtra(Component.EXTRA_VISIBILITY, false);
        if (count == DEFAULT_LAUNCH_COUNT) {
            Log.e(TAG, "Component launch count is missing");
            return;
        }

        final ContentValues values = new ContentValues(10);
        values.put(ActivityClassInfo.COUNT, count);
        values.put(ActivityClassInfo.VISIBILITY, visibility ? "visible" : "invisible");
        values.put(ActivityClassInfo.LAST_TIME, System.currentTimeMillis());
        onHandleIntent(intent, values);
    }

}
