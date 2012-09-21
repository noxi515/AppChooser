package jp.co.noxi.apppicker.content;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class ComponentComparator implements Comparator<Component> {

	public enum Order {

		/**
		 * Sort by frequency ascending
		 */
		FREQUENCY_ASC(0),
		/**
		 * Sort by frequency descending
		 */
		FREQUENCY_DESC(100),
		/**
		 * Sort by app's name ascending
		 */
		NAME_ASC(1),
		/**
		 * Sort by app's name descending
		 */
		NAME_DESC(101),
		/**
		 * Sort by history ascending
		 */
		HISTORY_ASC(2),
		/**
		 * Sort by history descending
		 */
		HISTORY_DESC(102);
		//		/**
		//		 * Sort by Android's default order ascending
		//		 */
		//		DEFAULT_ASC(3),
		//		/**
		//		 * Sort by Android's default order descending
		//		 */
		//		DEFAULT_DESC(103);

		/**
		 * ソート方法の設定値
		 */
		private final int value;

		private Order(int value) {
			this.value = value;
		}

		private static final int SORT_BY_FREQUENCY = 0;
		private static final int SORT_BY_NAME      = 1;
		private static final int SORT_BY_HISTORY   = 2;
		//		private static final int SORT_BY_DEFAULT   = 3;

		/**
		 * ソートメソッド選択ダイアログの結果から{@link Order}を取得する
		 * 
		 * @param value ソート番号
		 * @param asc true=昇順、false=降順
		 * @return
		 */
		public static Order convert(int value, boolean asc) {
			switch (value) {
			case SORT_BY_FREQUENCY:
				return asc ? FREQUENCY_ASC : FREQUENCY_DESC;

			case SORT_BY_NAME:
				return asc ? NAME_ASC : NAME_DESC;

			case SORT_BY_HISTORY:
				return asc ? HISTORY_ASC : HISTORY_DESC;

				//			case SORT_BY_DEFAULT:
				//				return asc ? DEFAULT_ASC : DEFAULT_DESC;

			default:
				throw new IllegalArgumentException("Unknown value => " + value);
			}
		}

		public static Order convert(int value) {
			if (value >= 100) {
				return convert(value - 100, false);
			} else {
				return convert(value, true);
			}
		}

		/**
		 * 昇順かどうか
		 * 
		 * @return 昇順=true、降順=false
		 */
		public static boolean isAscending(int value) {
			return value < 100;
		}

		public int getValue() {
			return value;
		}

		/**
		 * 昇順かどうか
		 * 
		 * @return 昇順=true、降順=false
		 */
		public boolean isAscending() {
			return value < 100;
		}

	}

	private final Collator mCollator = Collator.getInstance(Locale.getDefault());
	private Order mOrder;

	public ComponentComparator() {
		mOrder = Order.FREQUENCY_ASC;
	}

	public ComponentComparator(int value) {
		mOrder = Order.convert(value);
	}

	public ComponentComparator(Order order) {
		mOrder = order;
	}

	@Override
	public int compare(Component lhs, Component rhs) {
		switch (mOrder) {
		case FREQUENCY_ASC:
			return lhs.count == rhs.count ?
					mCollator.compare(lhs.label, rhs.label) : rhs.count - lhs.count;

		case FREQUENCY_DESC:
			return lhs.count == rhs.count ?
					mCollator.compare(rhs.label, lhs.label) : lhs.count - rhs.count;

		case NAME_ASC:
			return mCollator.compare(lhs.label, rhs.label);

		case NAME_DESC:
			return mCollator.compare(rhs.label, lhs.label);

		case HISTORY_ASC: {
			final int def = rhs.lastUseTime.compareTo(lhs.lastUseTime);
			if (def == 0) {
				return mCollator.compare(lhs.label, rhs.label);
			} else {
				return def;
			}
		}

		case HISTORY_DESC: {
			final int def = lhs.lastUseTime.compareTo(rhs.lastUseTime);
			if (def == 0) {
				return mCollator.compare(rhs.label, lhs.label);
			} else {
				return def;
			}
		}

		//		case DEFAULT_ASC:
		//			return lhs.preferOrder == rhs.preferOrder ?
		//					mCollator.compare(rhs.label, lhs.label) : lhs.preferOrder - rhs.preferOrder;
		//
		//		case DEFAULT_DESC:
		//			return lhs.preferOrder == rhs.preferOrder ?
		//					mCollator.compare(lhs.label, rhs.label) : rhs.preferOrder - lhs.preferOrder;

		default:
			return 0;
		}
	}

	/**
	 * ソート方法を取得する
	 */
	public Order getOrder() {
		return mOrder;
	}

	/**
	 * ソート方法を設定する
	 */
	public void setOrder(Order order) {
		mOrder = order;
	}

	/**
	 * ソート方法を設定する
	 * 
	 * @param value ソート方法
	 * @param asc true=昇順、false=降順
	 */
	public void setOrder(int value, boolean asc) {
		mOrder = Order.convert(value, asc);
	}

}
