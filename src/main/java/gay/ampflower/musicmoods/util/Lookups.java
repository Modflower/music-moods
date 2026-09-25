package gay.ampflower.musicmoods.util;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

/**
 * Unchecked lookup utilities.
 *
 * @author Ampflower
 * @since 0.7
 **/
@ApiStatus.Internal
public final class Lookups {
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	@SuppressWarnings("unchecked")
	public static <T> Class<T> classOf(final String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public static MethodHandles.Lookup lookup(String className) {
		return lookup(classOf(className));
	}

	public static MethodHandles.Lookup lookup(Class<?> direct) {
		try {
			return MethodHandles.privateLookupIn(direct, lookup);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public static MethodHandle findGetterIn(Class<?> owner, String name, Class<?> type) {
		try {
			return lookup(owner).findGetter(owner, name, type);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public static MethodHandle findGetterIn(MethodHandles.Lookup lookup, String name, Class<?> type) {
		try {
			return lookup.findGetter(lookup.lookupClass(), name, type);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public static MethodHandle canonicalConstructorOf(Class<?> owner) {
		try {
			final Constructor<?> construct;

			if (owner.isRecord()) {
				final RecordComponent[] components = owner.getRecordComponents();
				final Class[] classes = new Class[components.length];

				for (int i = 0; i < components.length; i++) {
					classes[i] = components[i].getType();
				}

				construct = owner.getDeclaredConstructor(classes);
			} else {
				construct = owner.getDeclaredConstructor();
			}

			construct.setAccessible(true);

			return lookup.unreflectConstructor(construct);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
}
