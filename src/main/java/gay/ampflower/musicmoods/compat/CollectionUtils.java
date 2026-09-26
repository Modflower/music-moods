package gay.ampflower.musicmoods.compat;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ampflower
 * @since 0.7
 **/
final class CollectionUtils {
	public static <T> boolean checkedCopy(
		final Collection<? super T> collection,
		final @Nullable Iterable<?> iterable,
		final Class<T> check
	) {
		if (iterable == null) {
			return false;
		}

		boolean flag = false;

		for (final var value : iterable) {
			if (check.isInstance(value)) {
				flag |= collection.add(check.cast(value));
			}
		}

		return flag;
	}

	public static <T> void checkedForEach(
		final @Nullable Iterable<?> iterable,
		final Class<T> check,
		final Consumer<? super T> consumer
	) {
		if (iterable == null) {
			return;
		}

		for (final var value : iterable) {
			if (check.isInstance(value)) {
				consumer.accept(check.cast(value));
			}
		}
	}

	public static <K, V> V checkedGet(
		final @Nullable Map<K, ?> map,
		final K key,
		final Class<V> check
	) {
		return checkedGetElse(map, key, check, null);
	}

	@Contract("_, _, _, !null -> !null")
	public static <K, V> V checkedGetElse(
		final @Nullable Map<? extends K, ?> map,
		final K key,
		final Class<V> check,
		final V fallback
	) {
		if (map == null) {
			return fallback;
		}

		final var value = map.get(key);
		if (check.isInstance(value)) {
			return check.cast(value);
		}

		return fallback;
	}

}
