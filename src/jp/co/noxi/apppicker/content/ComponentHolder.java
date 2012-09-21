package jp.co.noxi.apppicker.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.co.noxi.apppicker.content.ComponentComparator.Order;

/**
 * コンポーネント一覧を保持するクラス
 */
public final class ComponentHolder implements Clearable {

	private final List<Component> mComponents;
	private final List<Component> mVisibleComponents;
	private final ComponentComparator mComparator = new ComponentComparator();

	public ComponentHolder() {
		mComponents = new ArrayList<Component>();
		mVisibleComponents = new ArrayList<Component>();
	}

	public ComponentHolder(int size) {
		mComponents = new ArrayList<Component>(size);
		mVisibleComponents = new ArrayList<Component>(size);
	}

	public ComponentHolder(List<Component> components) {
		mComponents = components;
		mVisibleComponents = new ArrayList<Component>(components.size());
		for (Component component : components) {
			if (component.visibility) {
				mVisibleComponents.add(component);
			}
		}
	}

	@Override
	public void clear() {
		mComponents.clear();
		mVisibleComponents.clear();
	}

	/**
	 * コンポーネントを追加する
	 */
	public void add(Component component) {
		if (component != null) {
			mComponents.add(component);
			if (component.visibility) {
				mVisibleComponents.add(component);
			}
		}
	}

	/**
	 * コンポーネントを追加する
	 */
	public void addCollection(Collection<Component> collection) {
		for (Component component : collection) {
			mComponents.add(component);
			if (component.visibility) {
				mVisibleComponents.add(component);
			}
		}
	}

	/**
	 * コンポーネント一覧を取得する
	 */
	public List<Component> getComponents() {
		return mComponents;
	}

	/**
	 * 可視コンポーネント一覧を取得する
	 */
	public List<Component> getVisibleComponents() {
		return mVisibleComponents;
	}

	/**
	 * コンポーネントの並び替えを行う
	 * 
	 * @param order 並び替え方法
	 */
	public void sort(Order order) {
		mComparator.setOrder(order);
		Collections.sort(mComponents, mComparator);
		Collections.sort(mVisibleComponents, mComparator);
	}

	/**
	 * コンポーネントの可視性を変更する
	 * 
	 * @param position 変更するコンポーネントの位置
	 * @return 可視性を変更したコンポーネントオブジェクト
	 */
	public Component changeComponentVisibility(int position) {
		final Component component = mVisibleComponents.remove(position);
		component.visibility = !component.visibility;
		return component;
	}

	public int size() {
		return mVisibleComponents.size();
	}

	public Component get(int location) {
		return mVisibleComponents.get(location);
	}

	/**
	 * コンポーネントの可視性をリセットする
	 */
	public void resetVisibility() {
		mVisibleComponents.clear();
		for (Component component : mComponents) {
			if (!component.visibility) {
				component.visibility = true;
			}
			mVisibleComponents.add(component);
		}
	}

}
