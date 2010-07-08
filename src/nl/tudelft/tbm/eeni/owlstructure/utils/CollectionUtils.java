package nl.tudelft.tbm.eeni.owlstructure.utils;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionUtils {
	/**
	 * Returns all items that are both in collection1 and in collection2
	 */
	public static <T> Collection<T> intersectCollections(Collection<? extends T> collection1, Collection<? extends T> collection2) {
		Collection<T> result = new ArrayList<T>(collection1);
		result.retainAll(collection2);
		return result;
	}

	/**
	 * Returns all items in collection1 that are not in collection2
	 */
	public static <T> Collection<T> subtractCollections(Collection<? extends T> collection1, Collection<? extends T> collection2) {
		Collection<T> result = new ArrayList<T>(collection1);
		result.removeAll(collection2);
		return result;
	}
}