package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ActivityClassInfo;
import jp.co.noxi.apppicker.content.Component;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

/**
 * コンポーネントの見た目状態変更をデータベースに反映するタスク
 */
public final class ChangeComponentVisibilityTask extends ChangeComponentTask {

	public static void execute(Context context, Component component, String action, String mimeType) {
		if (component.packageName == null || component.className == null) {
			return;
		}

		final Intent intent = new Intent(context, ChangeComponentVisibilityTask.class);
		intent.putExtra(Component.EXTRA_ACTION, action);
		intent.putExtra(Component.EXTRA_PACKAGE_NAME, component.packageName);
		intent.putExtra(Component.EXTRA_CLASS_NAME, component.className);
		intent.putExtra(Component.EXTRA_VISIBILITY, component.visibility);
		intent.putExtra(Component.EXTRA_LABEL, component.label);
		if (mimeType != null) {
			intent.putExtra(Component.EXTRA_MIME_TYPE, mimeType);
		}
		context.startService(intent);
	}


	private static final String TAG = ChangeComponentVisibilityTask.class.getSimpleName();

	public ChangeComponentVisibilityTask() {
		super(TAG);
	}

	public ChangeComponentVisibilityTask(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		final boolean visibility = intent.getBooleanExtra(Component.EXTRA_VISIBILITY, false);
		final ContentValues values = new ContentValues(10);
		values.put(ActivityClassInfo.VISIBILITY, visibility ? "visible" : "invisible");
		onHandleIntent(intent, values);
	}

}
