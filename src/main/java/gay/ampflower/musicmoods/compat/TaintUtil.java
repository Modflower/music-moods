package gay.ampflower.musicmoods.compat;

import org.jetbrains.annotations.NotNullByDefault;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Ampflower
 * @since 0.7
 **/
@NotNullByDefault
final class TaintUtil {
	private static final String taintDesc = Type.getDescriptor(Taint.class);
	private static final String taintsDesc = Type.getDescriptor(Taints.class);

	@SafeVarargs
	private static <C extends Collection<? super T>, T> C populate(final C c, final T... array) {
		Collections.addAll(c, array);
		return c;
	}

	private static <C extends Collection<Type>> C asType(final C c, final Iterable<Class<?>> iterable) {
		for (final Class<?> clazz : iterable) {
			c.add(Type.getType(clazz));
		}
		return c;
	}

	private static <C extends Collection<Type>> C asType(final C c, final Class<?>... array) {
		for (final Class<?> clazz : array) {
			c.add(Type.getType(clazz));
		}
		return c;
	}

	public static AnnotationNode taintOfClass(
		final String session,
		final String target,
		final String mixin,
		final Class<?>... adjusters
	) {
		return taintOfType(session, target, mixin, asType(new ArrayList<>(), adjusters));
	}

	public static AnnotationNode taintOfClass(
		final String session,
		final String target,
		final String mixin,
		final Iterable<Class<?>> adjusters
	) {
		return taintOfType(session, target, mixin, asType(new ArrayList<>(), adjusters));
	}

	public static AnnotationNode taintOfType(
		final String session,
		final String target,
		final String mixin,
		final Type... adjusters
	) {
		return taintOfType(session, target, mixin, Arrays.asList(adjusters));
	}

	public static AnnotationNode taintOfType(
		final String session,
		final String target,
		final String mixin,
		final Collection<Type> adjusters
	) {
		final AnnotationNode node = new AnnotationNode(taintDesc);
		node.visit("session", session);
		node.visit("target", target);
		node.visit("mixin", mixin);
		node.visit("adjusters", new ArrayList<>(adjusters));
		node.visitEnd();
		return node;
	}

	private static AnnotationNode findOrMake(final List<AnnotationNode> list, final String desc) {
		for (final var node : list) {
			if (!desc.equals(node.desc)) {
				continue;
			}
			return node;
		}

		final AnnotationNode value = new AnnotationNode(desc);
		list.add(0, value);
		return value;
	}

	public static void addTaint(
		final MethodNode node,
		final AnnotationNode... value
	) {
		addTaint(node, Arrays.asList(value));
	}

	public static void addTaint(
		final MethodNode node,
		final Collection<AnnotationNode> value
	) {
		if (node.visibleAnnotations == null) {
			node.visibleAnnotations = new ArrayList<>();
		}

		final AnnotationNode taints = findOrMake(node.visibleAnnotations, taintsDesc);

		addRepeatable(taints, value);
	}

	public static void addTaint(
		final FieldNode node,
		final AnnotationNode... value
	) {
		addTaint(node, Arrays.asList(value));
	}

	public static void addTaint(
		final FieldNode node,
		final Collection<AnnotationNode> value
	) {
		if (node.visibleAnnotations == null) {
			node.visibleAnnotations = new ArrayList<>();
		}

		final AnnotationNode taints = findOrMake(node.visibleAnnotations, taintsDesc);

		addRepeatable(taints, value);
	}

	public static void addTaint(
		final ClassNode node,
		final AnnotationNode... value
	) {
		addTaint(node, Arrays.asList(value));
	}

	public static void addTaint(
		final ClassNode node,
		final Collection<AnnotationNode> value
	) {
		if (node.visibleAnnotations == null) {
			node.visibleAnnotations = new ArrayList<>();
		}

		final AnnotationNode taints = findOrMake(node.visibleAnnotations, taintsDesc);

		addRepeatable(taints, value);
	}

	public static void addTaint(
		final AnnotationNode node,
		final AnnotationNode... value
	) {
		addTaint(node, Arrays.asList(value));
	}

	public static void addTaint(
		final AnnotationNode node,
		final Collection<AnnotationNode> value
	) {
		if (!taintsDesc.equals(node.desc)) {
			throw new IllegalArgumentException("Not a Taints: " + node.desc);
		}

		addRepeatable(node, value);
	}

	private static void addRepeatable(
		final AnnotationNode node,
		final Collection<AnnotationNode> value
	) {
		if (node.values == null) {
			node.values = new ArrayList<>();
		}

		if ((node.values.size() & 1) != 0) {
			throw new IllegalArgumentException(node.desc + " => " + node.values);
		}

		final List<AnnotationNode> values;
		finder:
		{
			for (int i = 0; i < node.values.size(); i += 2) {
				if ("value".equals(node.values[i])) {
					if (!(node.values[i + 1] instanceof List<?> list)) {
						throw new IllegalArgumentException("Not a list: " + node.values[i + 1]);
					}

					values = (List<AnnotationNode>) list;
					break finder;
				}
			}

			values = new ArrayList<>();
			node.visit("value", values);
		}

		values.addAll(value);
	}
}
