package jp.co.noxi.apppicker.content;

/**
 * リソースを解放するためのメソッドを提供するインターフェース
 */
public interface Clearable {

	/**
	 * リソースを解放する
	 */
	void clear();
}
