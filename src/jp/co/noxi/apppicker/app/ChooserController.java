package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentHolder;
import android.content.Intent;

/**
 * ChooserDialogやConponentHolderにアクセスするためのインターフェス
 */
interface ChooserController {

    /**
     * 初回コンポーネント読み込み完了時
     * 
     * @param components
     *            コンポーネント一覧
     */
    void onComponentLoaded(ComponentHolder components);

    /**
     * ChooserDialogが表示されているかどうか
     */
    boolean isChooserDialogShowing();

    /**
     * ComponentHolderを取得する
     */
    ComponentLoaderHolder getComponentLoaderHolder();

    /**
     * ChooserDialogを取得する
     */
    ChooserDialog getChooserDialog();

    /**
     * Chooserを表示するためのIntentを取得する
     */
    Intent getChooserIntent();

    /**
     * DialogTitleを取得する
     */
    CharSequence getDialogTitle();

    /**
     * コンポーネントが選択された場合
     */
    void onComponentSelected(Component component, boolean multimode);

}
