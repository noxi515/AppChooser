package jp.co.noxi.apppicker.content;

import android.provider.BaseColumns;

public final class ActivityClassInfo implements BaseColumns {

	private ActivityClassInfo() {}

	public static final String TABLE_ACTIVITY = "activitylist";

	public static final String CLASS_NAME   = "className";
	public static final String PACKAGE_NAME = "packageName";
	public static final String LABEL        = "label";
	public static final String COUNT        = "count";
	public static final String VISIBILITY   = "visibility";
	public static final String ACTION       = "action";
	public static final String MIME_TYPE    = "type";
	public static final String LAST_TIME    = "lastTime";

}
