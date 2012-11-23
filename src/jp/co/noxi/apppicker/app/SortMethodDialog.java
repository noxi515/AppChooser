package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.ComponentComparator.Order;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * コンポーネントの並び替え方法を変更するダイアログ
 */
public final class SortMethodDialog extends Dialog
        implements View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener {

    /**
     * ソート方法選択ダイアログのリスナー
     */
    public interface OnSortMethodSelectedListener {
        /**
         * ソート方法が選択された時に呼ばれるメソッド
         * 
         * @param order
         *            ソート方法
         */
        public void onItemSelected(Order order);
    }

    public static Dialog newDialog(Activity activity,
            Order order, OnSortMethodSelectedListener listener) {
        if (order == null) {
            throw new NullPointerException("Order is null");
        }
        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }

        final TypedArray a = activity.getTheme()
                .obtainStyledAttributes(R.styleable.Theme);
        final int theme = a.getResourceId(
                R.styleable.Theme_dialogTheme, R.style.Theme_Dialog_Dark);
        a.recycle();

        return new SortMethodDialog(activity, theme, order.isAscending(), listener);
    }

    private final OnSortMethodSelectedListener mListener;
    private boolean mAscending;

    private RadioButton ascButton;
    private RadioButton descButton;

    private SortMethodDialog(Context context, int theme,
            boolean ascending, OnSortMethodSelectedListener listener) {
        super(context, theme);
        mListener = listener;
        mAscending = ascending;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sortmethod_dialog);

        // Set ClickListenr to Container View
        final View asc = findViewById(R.id.layout_ascending);
        asc.setOnClickListener(this);
        asc.setOnLongClickListener(this);
        final View desc = findViewById(R.id.layout_descending);
        desc.setOnClickListener(this);
        desc.setOnLongClickListener(this);

        ascButton = (RadioButton) asc.findViewById(R.id.radio_ascending);
        descButton = (RadioButton) desc.findViewById(R.id.radio_descending);
        if (mAscending) {
            ascButton.setChecked(true);
        } else {
            descButton.setChecked(true);
        }

        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(new ArrayAdapter<String>(getContext(),
                R.layout.sortmethod_dialog_text, R.id.text,
                getContext().getResources().getStringArray(R.array.sort)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        ascButton = null;
        descButton = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemSelected(Order.convert(position, mAscending));
        dismiss();
    }

    @Override
    public void onClick(View v) {
        final int containerId = v.getId();
        if (containerId == R.id.layout_ascending && !mAscending) {
            mAscending = true;
        } else if (containerId == R.id.layout_descending && mAscending) {
            mAscending = false;
        } else {
            return;
        }
        ascButton.setChecked(mAscending);
        descButton.setChecked(!mAscending);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.layout_ascending:
            case R.id.layout_descending:
                Toast.makeText(getContext(),
                        v.getContentDescription(), Toast.LENGTH_SHORT).show();
                return true;

            default:
                return false;
        }
    }

}
