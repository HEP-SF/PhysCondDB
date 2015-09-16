/**
 * 
 */
package conddb.utils.collections;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author aformic
 *
 */
public class CollectionUtils {

	public static <T> Collection<T> iterableToCollection(Iterable<T> iterable) {
		Collection<T> collection = new ArrayList<>();
		iterable.forEach(collection::add);
		return collection;
	}
}
