package jp.co.noxi.apppicker.app;

/**
 * コンポーネント一覧を保持するオブジェクトのインターフェース
 */
interface ComponentLoaderHolder {

    /**
     * コンポーネント一覧を取得する
     */
    void getComponents(ChooserDialog chooserDialog);
}
