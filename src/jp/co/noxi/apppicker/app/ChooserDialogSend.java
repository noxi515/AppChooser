package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.util.Common;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * 共有ダイアログUI
 */
public class ChooserDialogSend extends ChooserDialogImpl {

	private static final String SAVED_MULTIPLE_FLAG = Common.SAVED_SCHEME + "picker.send_multiple";

	private boolean mSendMultiple = false;

	private int mIconResIdSend;
	private int mIconResIdSendMultiple;

	public ChooserDialogSend() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final TypedArray a = getActivity()
				.getTheme().obtainStyledAttributes(R.styleable.Theme);
		mIconResIdSend = a.getResourceId(
				R.styleable.Theme_actionItemSend,
				R.drawable.ic_action_send_dark);
		mIconResIdSendMultiple = a.getResourceId(
				R.styleable.Theme_actionItemSendMultiple,
				R.drawable.ic_action_send_multiple_dark);
		a.recycle();

		if (savedInstanceState != null) {
			mSendMultiple = savedInstanceState.getBoolean(SAVED_MULTIPLE_FLAG);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final ViewGroup actionBar = (ViewGroup) view.findViewById(R.id.actionbar);
		final ImageButton btnSend = (ImageButton)
				inflater.inflate(R.layout.actionbutton, actionBar, false);
		btnSend.setId(R.id.button_send);
		btnSend.setImageResource(mSendMultiple ?
				mIconResIdSendMultiple : mIconResIdSend);
		btnSend.setContentDescription(getString(R.string.description_toggle_send_mode));
		btnSend.setOnClickListener(this);
		btnSend.setOnLongClickListener(this);
		actionBar.addView(btnSend);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_MULTIPLE_FLAG, mSendMultiple);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Component component = mComponents.get(position);
		component.count++;

		final Context context = getActivity();
		final ChooserController controller = (ChooserController) context;
		final Intent intent = controller.getChooserIntent();
		ChangeComponentCountTask.execute(context, component,
				intent.getAction(), intent.getType());
		controller.onComponentSelected(component, mSendMultiple);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.button_send) {
			mSendMultiple = !mSendMultiple;
			onSendModeClick((ImageButton) v);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.button_send) {
			Toast.makeText(getActivity(),
					v.getContentDescription(), Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return super.onLongClick(v);
		}
	}

	private void onSendModeClick(ImageButton button) {
		button.setImageResource(mSendMultiple ?
				mIconResIdSendMultiple : mIconResIdSend);
	}

}
