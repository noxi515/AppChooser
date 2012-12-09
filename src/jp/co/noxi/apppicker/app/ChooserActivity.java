package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentHolder;
import jp.co.noxi.apppicker.util.Common;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

public final class ChooserActivity extends FragmentActivity
        implements ChooserController, CheckPermission.OnClickListener {

    private static final String TAG = ChooserActivity.class.getSimpleName();

    /**
     * LoadComponentのフラグメントタグ
     */
    public static final String TAG_COMPONENT_HOLDER = Common.SAVED_SCHEME + "component";
    /**
     * ChooserDialogのフラグメントタグ
     */
    public static final String TAG_CHOOSER_DIALOG = Common.SAVED_SCHEME + "chooserdialog";
    /**
     * ChooserのIntentの保存キー
     */
    private static final String SAVED_STATE_CHOOSER_INTENT = Common.SAVED_SCHEME + "controller.intent";
    /**
     * ChooserのInitilalIntentsの保存キー
     */
    private static final String SAVED_STATE_CHOOSER_INITIAL_INTENT = Common.SAVED_SCHEME + "controller.initial_intent";
    /**
     * DialogTitleの保存キー
     */
    private static final String SAVED_STATE_DIALOG_TITLE = Common.SAVED_SCHEME + "controller.title";
    /**
     * コンポーネント数の保存キー
     */
    private static final String SAVED_STATE_COMPONENT_SIZE = Common.SAVED_SCHEME + "components.size";

    private static final String ARG_COMPONENT = "component";
    private static final String ARG_MULTI_MODE = "multimode";

    private ChooserDialog mChooserDialog;
    private ComponentLoaderHolder mComponentHolder;
    private ChooserControllerDetail mControllerDetail;
    private Intent mChooserIntent;
    private Intent[] mChooserInitialIntent;
    private CharSequence mDialogTitle;
    private int mComponentSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            if (Common.DEBUG) {
                Log.d(TAG, "--------------------------------------------------------------------------------");
                Log.d(TAG, "AppChooser init");
            }

            final Intent intent = getIntent();
            if (intent.hasExtra(Intent.EXTRA_TITLE)) {
                mDialogTitle = intent.getCharSequenceExtra(Intent.EXTRA_TITLE);
            } else {
                mDialogTitle = getString(R.string.default_chooser_title);
            }
            if (intent.hasExtra(Intent.EXTRA_INTENT)) {
                Parcelable parcel = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (parcel instanceof Intent) {
                    mChooserIntent = (Intent) parcel;
                } else {
                    mChooserIntent = new Intent(Intent.ACTION_MAIN, null);
                    mChooserIntent.addCategory(Intent.CATEGORY_DEFAULT);
                }
            } else {
                mChooserIntent = new Intent(intent);
            }
            mChooserIntent.setComponent(null);
            mChooserIntent.setPackage(null);

            if (intent.hasExtra(Intent.EXTRA_INITIAL_INTENTS)) {
                Parcelable[] pa = intent
                        .getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
                if (pa != null) {
                    mChooserInitialIntent = new Intent[pa.length];
                    for (int i = 0, length = pa.length; i < length; i++) {
                        if (pa[i] instanceof Intent) {
                            mChooserInitialIntent[i] = (Intent) pa[i];
                        }
                    }
                }
            }

            initChooserControllerDetail(mChooserIntent.getAction());
            mComponentHolder = mControllerDetail.newComponentLoaderHolder();
            if (!(mComponentHolder instanceof Fragment)) {
                Log.e(TAG, mComponentHolder.getClass().getName() + " must extend Fragment");
                finish();
            } else {
                fm.beginTransaction()
                        .add((Fragment) mComponentHolder, TAG_COMPONENT_HOLDER)
                        .commit();
            }
        } else {
            mComponentHolder = (ComponentLoaderHolder) fm.findFragmentByTag(TAG_COMPONENT_HOLDER);
            mChooserDialog = (ChooserDialog) fm.findFragmentByTag(TAG_CHOOSER_DIALOG);
            mChooserIntent = savedInstanceState.getParcelable(SAVED_STATE_CHOOSER_INTENT);
            mChooserInitialIntent = (Intent[]) savedInstanceState
                    .getParcelableArray(SAVED_STATE_CHOOSER_INITIAL_INTENT);
            mDialogTitle = savedInstanceState.getString(SAVED_STATE_DIALOG_TITLE);
            mComponentSize = savedInstanceState.getInt(SAVED_STATE_COMPONENT_SIZE);
            initChooserControllerDetail(mChooserIntent.getAction());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_STATE_CHOOSER_INTENT, mChooserIntent);
        if (mChooserInitialIntent != null) {
            outState.putParcelableArray(
                    SAVED_STATE_CHOOSER_INITIAL_INTENT, mChooserInitialIntent);
        }
        if (mDialogTitle != null) {
            outState.putCharSequence(SAVED_STATE_DIALOG_TITLE, mDialogTitle);
        }
        if (mComponentSize > 0) {
            outState.putInt(SAVED_STATE_COMPONENT_SIZE, mComponentSize);
        }
    }

    @Override
    public boolean isChooserDialogShowing() {
        if (mChooserDialog != null) {
            return true;
        } else {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_CHOOSER_DIALOG);
            if (f != null && f instanceof ChooserDialog) {
                mChooserDialog = (ChooserDialog) f;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onComponentLoaded(ComponentHolder components) {
        mComponentSize = components.size();
        if (mComponentSize == 0) {
            Toast.makeText(this, R.string.toast_no_target_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mChooserDialog = mControllerDetail.newChooserDialog();
        mChooserDialog.show(getSupportFragmentManager(), TAG_CHOOSER_DIALOG);
    }

    @Override
    public ComponentLoaderHolder getComponentLoaderHolder() {
        return mComponentHolder;
    }

    @Override
    public ChooserDialog getChooserDialog() {
        return mChooserDialog;
    }

    @Override
    public Intent getChooserIntent() {
        return mChooserIntent;
    }

    @Override
    public Intent[] getChooserInitialIntent() {
        return mChooserInitialIntent;
    }

    @Override
    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }

    @Override
    public void onComponentSelected(Component component, boolean multiMode) {
        try {
            if (CheckPermission.isGranted(this, component)) {
                mControllerDetail.onComponentSelected(component, multiMode);
            } else {
                final Bundle args = new Bundle();
                args.putParcelable(ARG_COMPONENT, component);
                args.putBoolean(ARG_MULTI_MODE, multiMode);
                CheckPermission.showDialog(this, args);
            }
        } catch (NameNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_target_found, Toast.LENGTH_LONG).show();
            onClickLaunchCancel(null);
        }
    }

    @Override
    public void onClickForceLaunch(Bundle args) {
        if (args == null) {
            return;
        }

        final Component component = args.getParcelable(ARG_COMPONENT);
        final boolean multiMode = args.getBoolean(ARG_MULTI_MODE);
        try {
            mControllerDetail.onComponentSelected(component, multiMode);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.permission_denial, Toast.LENGTH_LONG).show();
            onClickLaunchCancel(args);
        }
    }

    @Override
    public void onClickLaunchCancel(Bundle args) {
        if (mComponentSize == 1) {
            finish();
        }
    }

    private void initChooserControllerDetail(String action) {
        final int hash = action.hashCode();

        // ACTION_SEND
        if (Intent.ACTION_SEND.hashCode() == hash
                || Intent.ACTION_SEND_MULTIPLE.hashCode() == hash
                || Intent.ACTION_SENDTO.hashCode() == hash) {
            mControllerDetail = new SendControllerDetail();
        }
        // DEFAULT
        else {
            mControllerDetail = new DefaultControllerDetail();
        }
    }

    /**
     * コントローラーの内部分岐インターフェース
     */
    private interface ChooserControllerDetail {
        /**
         * ComponentLoaderを選択
         */
        ComponentLoaderHolder newComponentLoaderHolder();

        /**
         * ChooserDialogを選択
         */
        ChooserDialog newChooserDialog();

        /**
         * アイテムが選択された際の挙動
         */
        void onComponentSelected(Component component, boolean multiMode);
    }

    /**
     * デフォルトのコントローラー動作
     */
    private class DefaultControllerDetail implements ChooserControllerDetail {
        @Override
        public ComponentLoaderHolder newComponentLoaderHolder() {
            return new LoadComponentFragment();
        }

        @Override
        public ChooserDialog newChooserDialog() {
            return new ChooserDialogImpl();
        }

        @Override
        public void onComponentSelected(Component component, boolean multiMode) {
            final Intent target = component.getIntent(mChooserIntent);
            if (component.packageName != null && component.className != null
                    && component.className.length() > 0) {
                target.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                target.setComponent(new ComponentName(component.packageName, component.className));
                startActivity(target);
                setResult(RESULT_OK);
            } else {
                setResult(RESULT_OK, target);
            }
            finish();
        }
    }

    /**
     * ACTION_SENDのコントローラー動作
     */
    private class SendControllerDetail implements ChooserControllerDetail {
        @Override
        public ComponentLoaderHolder newComponentLoaderHolder() {
            return new LoadComponentFragment();
        }

        @Override
        public ChooserDialog newChooserDialog() {
            return new ChooserDialogSend();
        }

        @Override
        public void onComponentSelected(Component component, boolean multiMode) {
            final Intent target = component.getIntent(mChooserIntent);
            if (component.packageName != null && component.className != null
                    && component.className.length() > 0) {
                target.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                target.setComponent(new ComponentName(component.packageName, component.className));
                startActivity(target);
                setResult(RESULT_OK);
            } else {
                setResult(RESULT_OK, target);
            }

            if (!multiMode) {
                finish();
            }
        }
    }

}
