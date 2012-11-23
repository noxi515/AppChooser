package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * チェック項目付きのダイアログクラス
 */
public final class CheckableDialog extends AlertDialog {

    public interface OnClickListener {
        /**
         * @param checked
         *            チェックボックスの状態
         */
        public void onClick(boolean checked);
    }

    private CharSequence mMessage;
    private CharSequence mCheckBoxMessage;
    private boolean mChecked = false;

    public CheckableDialog(Context context) {
        super(context);
        setView(LayoutInflater.from(context).inflate(R.layout.dialog_allow, null, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mMessage != null) {
            ((TextView) findViewById(R.id.message)).setText(mMessage);
        }
        if (mCheckBoxMessage != null) {
            ((TextView) findViewById(R.id.checkbox_message)).setText(mCheckBoxMessage);
        }
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setChecked(mChecked);
        findViewById(R.id.layout_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecked = !mChecked;
                checkBox.setChecked(mChecked);
            }
        });
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setPositiveButton(int resId,
            final CheckableDialog.OnClickListener listener) {
        final DialogInterface.OnClickListener l = listener == null ? null :
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(mChecked);
                    }
                };
        setButton(BUTTON_POSITIVE, getContext().getString(resId), l);
    }

    public void setNegativeButton(int resId,
            final CheckableDialog.OnClickListener listener) {
        final DialogInterface.OnClickListener l = listener == null ? null :
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(mChecked);
                    }
                };
        setButton(BUTTON_NEGATIVE, getContext().getString(resId), l);
    }

    @Override
    public void setMessage(CharSequence message) {
        mMessage = message;
    }

    public void setCheckMessage(CharSequence message) {
        mCheckBoxMessage = message;
    }

}
