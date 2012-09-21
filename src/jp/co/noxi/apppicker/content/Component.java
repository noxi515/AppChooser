package jp.co.noxi.apppicker.content;

import java.util.Date;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class Component {

	private static final String INTENT_EXTRA_HEADER = "noxi.picker.component.";

	/**
	 * 対象コンポーネントを取得したIntentAction
	 */
	public static final String EXTRA_ACTION = INTENT_EXTRA_HEADER + "action";
	/**
	 * 対象コンポーネントを取得したデータタイプ
	 */
	public static final String EXTRA_MIME_TYPE = INTENT_EXTRA_HEADER + "dataType";
	/**
	 * コンポーネントのパッケージ名
	 */
	public static final String EXTRA_PACKAGE_NAME = INTENT_EXTRA_HEADER + "packageName";
	/**
	 * コンポーネントのクラス名
	 */
	public static final String EXTRA_CLASS_NAME = INTENT_EXTRA_HEADER + "className";
	/**
	 * コンポーネントのラベル
	 */
	public static final String EXTRA_LABEL = INTENT_EXTRA_HEADER + "label";
	/**
	 * コンポーネントの可視性
	 */
	public static final String EXTRA_VISIBILITY = INTENT_EXTRA_HEADER + "visibility";
	/**
	 * コンポーネントの起動回数
	 */
	public static final String EXTRA_LAUNCH_COUNT = INTENT_EXTRA_HEADER + "count";

	/**
	 * 対象パッケージ名
	 */
	public String packageName;
	/**
	 * 対象Activityのクラス名
	 */
	public String className;

	/**
	 * コンポーネントの選択回数
	 */
	public int count = 0;
	/**
	 * コンポーネントの可視状態
	 */
	public boolean visibility = true;
	/**
	 * 最後に選択された時間
	 */
	public Date lastUseTime = new Date(-1);

	public CharSequence label = null;
	public Drawable icon = null;
	//	public Bundle extras;

	public Component(CharSequence label, Drawable icon) {
		this(label, icon, 0, true);
	}

	public Component(CharSequence label, Drawable icon, int count, boolean visibility) {
		this.label = label;
		this.icon = icon;
		this.count = count;
		this.visibility = visibility;

		if (label == null) {
			label = "";
		}
	}

	public Component(PackageManager pm, ResolveInfo resolveInfo) {
		this(pm, resolveInfo, 0, true);
	}

	public Component(PackageManager pm, ResolveInfo ri, int count, boolean visibility) {
		packageName = ri.activityInfo.applicationInfo.packageName;
		className = ri.activityInfo.name;
		label = ri.loadLabel(pm);
		icon = ri.loadIcon(pm);
		this.count = count;
		this.visibility = visibility;

		if (label == null) {
			if (ri.activityInfo != null) {
				label = ri.activityInfo.name;
			} else {
				label = "";
			}
		}
	}

	public final Intent getIntent(Intent baseIntent) {
		Intent intent = new Intent(baseIntent);
		if (packageName != null && className != null && className.length() > 0) {
			intent.setClassName(packageName, className);
			//			if (extras != null) {
			//				intent.putExtras(extras);
			//			}
		} else {
			intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
		}
		return intent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (className == null ? 0 : className.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result
				+ (packageName == null ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Component other = (Component) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null) {
				return false;
			}
		} else if (!packageName.equals(other.packageName)) {
			return false;
		}
		return true;
	}

}
