package jp.co.noxi.apppicker.widget;

import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentHolder;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * Componentのリスト、グリッドアダプター
 */
public abstract class ComponentAdapter extends BaseAdapter {

	public interface ViewBinder {
	}

	final Context mContext;
	final LayoutInflater mInflater;
	final int mLayoutResId;

	ComponentHolder mComponents;

	public ComponentAdapter(Activity activity,
			int layoutResId, ComponentHolder components) {
		mContext = activity.getApplicationContext();
		mLayoutResId = layoutResId;
		mComponents = components;
		mInflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {
		if (mComponents == null) {
			return AdapterView.INVALID_POSITION;
		} else {
			return mComponents.size();
		}
	}

	@Override
	public Component getItem(int position) {
		if (mComponents == null) {
			return null;
		} else {
			return mComponents.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		if (mComponents == null) {
			return AdapterView.INVALID_ROW_ID;
		} else {
			return position;
		}
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		ViewBinder binder;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutResId, parent, false);
			binder = newView(convertView);
			convertView.setTag(binder);
		} else {
			binder = (ViewBinder) convertView.getTag();
		}

		bindView(mComponents.get(position), binder, position);
		return convertView;
	}

	/**
	 * コンポーネント一覧を設定する
	 */
	public void setComponents(ComponentHolder components) {
		mComponents = components;
	}

	/**
	 * コンポーネント一覧を取得する
	 */
	public ComponentHolder getComponents() {
		return mComponents;
	}

	protected abstract ViewBinder newView(View convertView);

	protected abstract void bindView(
			Component component, ViewBinder binder, int position);

}
