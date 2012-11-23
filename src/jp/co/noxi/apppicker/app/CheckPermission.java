package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * 対象コンポーネントの権限要求チェック、権限通知ダイアログのクラス
 */
public final class CheckPermission extends DialogFragment {

    /**
     * 対象コンポーネントに権限要求が存在するかをチェックする。
     */
    public static boolean isGranted(Context context,
            Component component) throws NameNotFoundException {
        ActivityInfo ai = context.getPackageManager().getActivityInfo(
                new ComponentName(component.packageName, component.className),
                PackageManager.GET_PERMISSIONS);
        return ai.permission == null;
    }

    private static final String TAG_COMMIT = "dialog.check_permission";

    /**
     * 権限不足通知ダイアログの表示
     */
    public static void showDialog(FragmentActivity activity, Bundle args) {
        showDialog(activity, activity.getSupportFragmentManager(), args);
    }

    /**
     * 権限不足通知ダイアログの表示
     */
    public static void showDialog(Fragment fragment, Bundle args) {
        showDialog(fragment, fragment.getChildFragmentManager(), args);
    }

    private static void showDialog(Object parent, FragmentManager manager, Bundle args) {
        if (!(parent instanceof CheckPermission.OnClickListener)) {
            throw new ClassCastException(parent.getClass().getSimpleName()
                    + " must implements OnClickListener");
        }
        DialogFragment dialog = new CheckPermission();
        dialog.setCancelable(false);
        if (args != null) {
            dialog.setArguments(args);
        }
        dialog.show(manager, TAG_COMMIT);
    }

    /**
     * 権限不足ダイアログのクリックリスナー
     */
    public interface OnClickListener {
        /**
         * 無理やり起動してみる場合
         */
        public void onClickForceLaunch(Bundle args);

        /**
         * 起動をキャンセルする場合
         */
        public void onClickLaunchCancel(Bundle args);
    }

    private CheckPermission.OnClickListener mListener;

    public CheckPermission() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment() == null) {
            mListener = (CheckPermission.OnClickListener) getActivity();
        } else {
            mListener = (CheckPermission.OnClickListener) getParentFragment();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_permission_lacked)
                .setMessage(R.string.maybe_permission_lacked)
                .setPositiveButton(R.string.button_force_launch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClickForceLaunch(getArguments());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClickLaunchCancel(getArguments());
                    }
                })
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onClickLaunchCancel(getArguments());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }
}
