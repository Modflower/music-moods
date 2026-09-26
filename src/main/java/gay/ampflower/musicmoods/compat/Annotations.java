package gay.ampflower.musicmoods.compat;

import gay.ampflower.musicmoods.util.Lookups;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ampflower
 * @since 0.7
 **/
final class Annotations {
	// region from field/method/class
	public static List<AnnotationNode> findAnnotations(
		final FieldNode node,
		final String annotation
	) {
		return findAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, new ArrayList<>());
	}

	public static List<AnnotationNode> findAnnotations(
		final MethodNode node,
		final String annotation
	) {
		return findAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, new ArrayList<>());
	}

	public static List<AnnotationNode> findAnnotations(
		final ClassNode node,
		final String annotation
	) {
		return findAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, new ArrayList<>());
	}

	public static <C extends Collection<AnnotationNode>> C findAnnotations(
		final @Nullable List<AnnotationNode> visibleAnnotations,
		final @Nullable List<AnnotationNode> invisibleAnnotations,
		final String annotation,
		final C annotations
	) {
		iterateAnnotations(visibleAnnotations, invisibleAnnotations, annotation, annotations::add);
		return annotations;
	}

	public static void iterateAnnotations(
		final FieldNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateAnnotations(
		final MethodNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateAnnotations(
		final ClassNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateAnnotations(
		final @Nullable List<AnnotationNode> visibleAnnotations,
		final @Nullable List<AnnotationNode> invisibleAnnotations,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		if (visibleAnnotations != null) {
			for (final var value : visibleAnnotations) {
				if (annotation.equals(value.desc)) {
					consumer.accept(value);
				}
			}
		}

		if (invisibleAnnotations != null) {
			for (final var value : invisibleAnnotations) {
				if (annotation.equals(value.desc)) {
					consumer.accept(value);
				}
			}
		}
	}

	public static List<AnnotationNode> findRepeatableAnnotations(
		final FieldNode node,
		final String annotation
	) {
		return findRepeatableAnnotations(
			node.visibleAnnotations,
			node.invisibleAnnotations,
			annotation,
			new ArrayList<>()
		);
	}

	public static List<AnnotationNode> findRepeatableAnnotations(
		final MethodNode node,
		final String annotation
	) {
		return findRepeatableAnnotations(
			node.visibleAnnotations,
			node.invisibleAnnotations,
			annotation,
			new ArrayList<>()
		);
	}

	public static List<AnnotationNode> findRepeatableAnnotations(
		final ClassNode node,
		final String annotation
	) {
		return findRepeatableAnnotations(
			node.visibleAnnotations,
			node.invisibleAnnotations,
			annotation,
			new ArrayList<>()
		);
	}

	public static <C extends Collection<AnnotationNode>> C findRepeatableAnnotations(
		final @Nullable List<AnnotationNode> visibleAnnotations,
		final @Nullable List<AnnotationNode> invisibleAnnotations,
		final String annotation,
		final C annotations
	) {
		iterateRepeatableAnnotations(visibleAnnotations, invisibleAnnotations, annotation, annotations::add);
		return annotations;
	}

	public static void iterateRepeatableAnnotations(
		final FieldNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateRepeatableAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateRepeatableAnnotations(
		final MethodNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateRepeatableAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateRepeatableAnnotations(
		final ClassNode node,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateRepeatableAnnotations(node.visibleAnnotations, node.invisibleAnnotations, annotation, consumer);
	}

	public static void iterateRepeatableAnnotations(
		final @Nullable List<AnnotationNode> visibleAnnotations,
		final @Nullable List<AnnotationNode> invisibleAnnotations,
		final String annotation,
		final Consumer<AnnotationNode> consumer
	) {
		iterateAnnotations(visibleAnnotations, invisibleAnnotations, annotation, consumer);

		final Type container = getRepeatableContainer(Type.getType(annotation));
		if (container == null) {
			return;
		}

		iterateAnnotations(
			visibleAnnotations,
			invisibleAnnotations,
			container.descriptor,
			value -> filteredRepeatable(value, annotation, consumer)
		);
	}

	//endregion

	public static Map<String, ?> toMap(
		final AnnotationNode node
	) {
		if (node.values == null || node.values.isEmpty) {
			return Map.of();
		}

		final Map.Entry<String, ?>[] entries = new Map.Entry[node.values.size() >> 1];

		for (int i = 0; i < node.values.size(); i += 2) {
			entries[i >> 1] = Map.entry((String) node.values[i], node.values[i + 1]);
		}

		return Map.ofEntries(entries);
	}

	public static <T> @Nullable T find(
		final AnnotationNode node,
		final String name,
		final Class<T> type
	) {
		for (int i = 0; i < node.values.size(); i += 2) {
			if (name.equals(node.values[i])) {
				final var value = node.values[i + 1];

				if (!type.isInstance(value)) {
					throw new IllegalArgumentException("Not a " + type + ": " + value);
				}

				return type.cast(value);
			}
		}

		return null;
	}

	public static <T> T findOrCreate(
		final AnnotationNode node,
		final String name,
		final Class<T> type,
		final Supplier<T> creator
	) {
		{
			final var value = find(node, name, type);
			if (value != null) {
				return value;
			}
		}

		final var value = creator.get();
		node.visit(name, value);
		return value;
	}

	public static void filteredRepeatable(
		final AnnotationNode container,
		final String descriptor,
		final Consumer<AnnotationNode> consumer
	) {
		final List<?> list = find(container, "value", List.class);
		if (list == null) {
			return;
		}
		for (final var sub : list) {
			if (sub instanceof AnnotationNode subNode && descriptor.equals(subNode.desc)) {
				consumer.accept(subNode);
			}
		}
	}

	public static List<AnnotationNode> filteredRepeatable(
		final AnnotationNode container,
		final String descriptor
	) {
		final List<AnnotationNode> annotations = new ArrayList<>();
		filteredRepeatable(container, descriptor, annotations::add);
		return annotations;
	}


	public static Type getRepeatableContainer(final Type annotation) {
		final ClassNode node = loadScaffolding(annotation.internalName);
		final List<AnnotationNode> annotations = findAnnotations(node, "Ljava/lang/annotation/Repeatable;");

		for (final AnnotationNode value : annotations) {
			final Type container = find(value, "value", Type.class);

			if (container != null) {
				return container;
			}
		}

		return null;
	}

	public static ClassNode loadScaffolding(final String className) {
		final String path = "/" + className.replace('.', '/') + ".class";

		final ClassNode classNode = new ClassNode();

		try (final InputStream stream = Annotations.class.getResourceAsStream(path)) {
			if (stream == null) {
				throw new IllegalArgumentException("No such class: " + path);
			}
			new ClassReader(stream).accept(
				classNode,
				ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
			);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return classNode;
	}

	/**
	 * Coercing annotation nodes into a fully fledged record? Impossible!
	 */
	public static <T extends Record> T load(
		final Class<T> record,
		final AnnotationNode annotation
	) {
		assert record.isRecord() : record;

		final RecordComponent[] components = record.getRecordComponents();
		final Object[] construct = new Object[components.length];

		final Map<String, ?> map = toMap(annotation);

		for (int i = 0; i < components.length; i++) {
			final RecordComponent component = components[i];
			final var value = map.get(component.getName());

			construct[i] = coerce(component.getType(), component.getGenericType(), value);
		}

		try {
			return (T) Lookups.canonicalConstructorOf(record).invokeWithArguments(construct);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}

	private static final Map<Class<?>, Function> remapper;

	static {
		final var rawReampper = new HashMap<Class<?>, Function>();

		rawReampper.put(Optional.class, Optional::ofNullable);
		rawReampper.put(
			OptionalInt.class,
			value -> value instanceof Integer i ? OptionalInt.of(i) : OptionalInt.empty()
		);
		rawReampper.put(
			OptionalDouble.class,
			value -> value instanceof Double d ? OptionalDouble.of(d) : OptionalDouble.empty()
		);

		remapper = Map.copyOf(rawReampper);
	}

	private static <T> T coerce(Class<T> clazz, java.lang.reflect.Type type, Object value) {
		if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}

		if (value != null && clazz == Optional.class && type instanceof ParameterizedType parameterizedType) {
			final var expected = toClass(parameterizedType.getActualTypeArguments()[0]);

			if (expected.isEnum()) {
				value = Enum.valueOf((Class) expected, (String) value);
			}

			// explicit checkcast
			expected.cast(value);
		}

		return (T) remapper.get(clazz).apply(value);
	}

	private static Class<?> toClass(final java.lang.reflect.Type type) {
		if (type instanceof Class<?> clazz) {
			return clazz;
		}
		if (type instanceof ParameterizedType parameterizedType) {
			return toClass(parameterizedType.getRawType());
		}
		throw new ClassCastException(type.getClass() + " can't be toClass'd; is: " + type);
	}
}
