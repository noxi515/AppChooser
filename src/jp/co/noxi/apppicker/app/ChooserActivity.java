package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentHolder;
import jp.co.noxi.apppicker.util.Common;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

public final class ChooserActivity extends FragmentActivity implements ChooserController {

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
     * DialogTitleの保存キー
     */
    private static final String SAVED_STATE_DIALOG_TITLE = Common.SAVED_SCHEME + "controller.title";

    private ChooserDialog mChooserDialog;
    private ComponentLoaderHolder mComponentHolder;
    private ChooserControllerDetail mControllerDetail;
    private Intent mChooserIntent;
    private CharSequence mDialogTitle;

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
            mDialogTitle = savedInstanceState.getString(SAVED_STATE_DIALOG_TITLE);
            initChooserControllerDetail(mChooserIntent.getAction());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_STATE_CHOOSER_INTENT, mChooserIntent);
        if (mDialogTitle != null) {
            outState.putCharSequence(SAVED_STATE_DIALOG_TITLE, mDialogTitle);
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
        if (components.getComponents().size() == 0) {
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
    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }

    @Override
    public void onComponentSelected(Component component, boolean multiMode) {
        mControllerDetail.onComponentSelected(component, multiMode);
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
