package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentComparator.Order;
import jp.co.noxi.apppicker.content.ComponentHolder;
import jp.co.noxi.apppicker.util.Common;
import jp.co.noxi.apppicker.widget.ComponentAdapter;
import jp.co.noxi.apppicker.widget.ComponentAdapterFroyo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 通常の選択ダイアログUI
 */
public class ChooserDialogImpl extends DialogFragment
implements ChooserDialog, View.OnClickListener, View.OnLongClickListener,
AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	private static final String TAG = ChooserDialogImpl.class.getSimpleName();

	public static final String PREF_APPPICKER_SORT_ORDER = "apppicker.settings.sort";
	public static final String PREF_SHOW_DIALOG_INVISIBLE = "apppicker.settings.invisible_dialog";

	public static final String EXTRA_DIALOG_TITLE = "dialogTitle";
	public static final String EXTRA_INTENT = "intent";

	SharedPreferences mPref;

	Order mOrder;

	boolean mInvisibleDialog;

	TextView mTitle;
	ListView mListView;

	Dialog mDialog;

	ComponentAdapter mListAdapter;
	ComponentHolder mComponents;

	public ChooserDialogImpl() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		final TypedArray a = getActivity()
				.getTheme().obtainStyledAttributes(R.styleable.Theme);
		if (savedInstanceState == null) {
			setStyle(STYLE_NO_TITLE, a.getResourceId(
					R.styleable.Theme_dialogTheme, R.style.Theme_Dialog_Dark));
		}
		mOrder = Order.convert(mPref.getInt(
				PREF_APPPICKER_SORT_ORDER, Order.FREQUENCY_ASC.getValue()));
		mInvisibleDialog = mPref.getBoolean(PREF_SHOW_DIALOG_INVISIBLE, true);
		a.recycle();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new Dialog(getActivity(), getTheme()) {
			@Override
			public void onBackPressed() {
				if (!ChooserDialogImpl.this.onBackPressed()) {
					super.onBackPressed();
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.chooserdialog, container, false);
		final View sortButton = view.findViewById(R.id.button_sort);
		sortButton.setOnClickListener(this);
		sortButton.setOnLongClickListener(this);

		final ImageButton btnRestore = (ImageButton)
				view.findViewById(R.id.button_restore);
		btnRestore.setOnClickListener(this);
		btnRestore.setOnLongClickListener(this);

		mTitle = (TextView) view.findViewById(R.id.actionbar_title);

		mListView = (ListView) view.findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Resources res = getResources();
		final int widthScale = res.getInteger(R.integer.chooserdialog_width);
		if (widthScale > 0) {
			final Window window = getDialog().getWindow();
			final WindowManager.LayoutParams params = window.getAttributes();
			params.width = res.getDisplayMetrics().widthPixels * widthScale / 100;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			window.setAttributes(params);
		}

		mListAdapter = new ComponentAdapterFroyo(getActivity(), R.layout.item_list, null);
		mListView.setAdapter(mListAdapter);
		mTitle.setText(((ChooserController) getActivity()).getDialogTitle());
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 * 
	 * Dialog表示時にComponentHolderへ一覧取得しにいく
	 */
	@Override
	public void onResume() {
		super.onResume();
		((ChooserController) getActivity()).getComponentLoaderHolder().getComponents(this);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 * 
	 * 一時Dialogの破棄と持っている情報の保存
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}

		mPref.edit()
		.putInt(PREF_APPPICKER_SORT_ORDER, mOrder.getValue())
		.putBoolean(PREF_SHOW_DIALOG_INVISIBLE, mInvisibleDialog)
		.commit();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onDestroyView()
	 * 
	 * Viewへの参照破棄
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mTitle = null;
		mListView = null;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * ActionButtonのクリック >> 各ボタンアクション
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_sort:
			onSortClick((ImageButton) v);
			break;

		case R.id.button_restore:
			onRestoreClick((ImageButton) v);
			break;
		}
	}

	@Override
	public boolean onBackPressed() {
		if (mComponents != null) {
			mListAdapter.setComponents(null);
			mComponents.clear();
			mComponents = null;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 * 
	 * ActionButtonのロングクリック >> ContentDescription表示
	 */
	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.button_sort:
		case R.id.button_restore:
			Toast.makeText(getActivity(),
					v.getContentDescription(), Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(
	 *              android.widget.AdapterView, android.view.View, int, long)
	 * 
	 * Component選択時
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Component component = mComponents.get(position);
		component.count++;

		final Context context = getActivity();
		final ChooserController controller = (ChooserController) context;
		final Intent intent = controller.getChooserIntent();
		ChangeComponentCountTask.execute(context, component,
				intent.getAction(), intent.getType());
		controller.onComponentSelected(component, false);
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(
	 *              android.widget.AdapterView, android.view.View, int, long)
	 * 
	 * Component長押し時
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		final Component component = mComponents.get(position);
		if (component.packageName == null || component.className == null) {
			mDialog = new AlertDialog.Builder(getActivity())
			.setMessage(getString(R.string.message_cannot_register_database, component.label))
			.setPositiveButton(android.R.string.ok, null)
			.create();
			mDialog.show();
		} else if (mInvisibleDialog) {
			final String message = getString(
					R.string.message_change_component_invisible, component.label);
			final CheckableDialog dialog = new CheckableDialog(getActivity());
			dialog.setMessage(message);
			dialog.setPositiveButton(android.R.string.ok,
					new CheckableDialog.OnClickListener() {
				@Override
				public void onClick(boolean checked) {
					if (checked) {
						mInvisibleDialog = false;
					}
					changeComponentVisibility(position);
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			mDialog = dialog;
			dialog.show();
		} else {
			changeComponentVisibility(position);
		}
		return true;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		final Activity activity = getActivity();
		if (activity != null && !activity.isFinishing()) {
			activity.finish();
		}
	}

	/**
	 * コンポーネントの変更時
	 */
	public void onDataSetChanged() {
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * ソートボタンのクリックイベント
	 * 
	 * @param button ソートボタン
	 */
	protected void onSortClick(ImageButton button) {
		mDialog = SortMethodDialog.newDialog(getActivity(), mOrder,
				new SortMethodDialog.OnSortMethodSelectedListener() {
			@Override
			public void onItemSelected(Order order) {
				mOrder = order;
				if (mComponents != null) {
					mComponents.sort(order);
					onDataSetChanged();
				}
			}
		});
		mDialog.show();
	}

	/**
	 * 復元ボタンのクリックイベント
	 * 
	 * @param button 復元ボタン
	 */
	protected void onRestoreClick(ImageButton button) {
		final Dialog dialog = mDialog = new AlertDialog.Builder(getActivity())
		.setTitle(R.string.title_restore_items)
		.setMessage(R.string.message_restore_items)
		.setPositiveButton(R.string.restore_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Activity activity = getActivity();
				final Intent intent = ((ChooserController) activity).getChooserIntent();
				ResetComponentVisibilityTask.execute(
						activity, intent.getAction(), intent.getType());

				mComponents.resetVisibility();
				onDataSetChanged();
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		dialog.show();
	}

	@Override
	public void onComponentLoaded(ComponentHolder components) {
		if (components != null) {
			mComponents = components;
			mComponents.sort(mOrder);

			mListAdapter.setComponents(components);
			onDataSetChanged();

			if (Common.DEBUG) {
				Log.d(TAG, "AppChooser displayed");
				Log.d(TAG, "--------------------------------------------------------------------------------");
			}
		}
	}

	protected void changeComponentVisibility(int position) {
		final Context context = getActivity();
		final Intent intent = ((ChooserController) context).getChooserIntent();
		ChangeComponentVisibilityTask.execute(context,
				mComponents.changeComponentVisibility(position),
				intent.getAction(), intent.getType());
		onDataSetChanged();
	}

}
