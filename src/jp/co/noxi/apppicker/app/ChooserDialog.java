package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ComponentHolder;
import android.support.v4.app.FragmentManager;

interface ChooserDialog {

    /**
     * コンポーネント一覧を設定する
     */
    void onComponentLoaded(ComponentHolder components);

    /**
     * ChooserDialogを表示する
     */
    void show(FragmentManager fm, String tag);

    /**
     * バックキーが押されたことを通知する
     * 
     * @return TRUEの時ChooserDialogは何らかの処理を行う為Activityは何もしない
     */
    boolean onBackPressed();
}
