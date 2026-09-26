package gay.ampflower.musicmoods.compat;

import gay.ampflower.musicmoods.util.Lookups;
import org.jetbrains.annotations.NotNullByDefault;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.service.MixinService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Ampflower
 * @since 0.7
 **/
@NotNullByDefault
final class MixinSupport {
	private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	// region Mixin Internals

	// Externalisation of internals?
	// Entangling an extension instead.
	private static final Class<?> targetClassContext = Lookups.classOf(
		"org.spongepowered.asm.mixin.transformer.TargetClassContext");
	private static final Class<?> mixinInfo = Lookups.classOf("org.spongepowered.asm.mixin.transformer.MixinInfo");
	private static final Class<?> mixinState = Lookups.classOf("org.spongepowered.asm.mixin.transformer.MixinInfo$State");

	private static final MethodHandles.Lookup targetClassContextLookup = Lookups.lookup(targetClassContext);
	private static final MethodHandles.Lookup mixinInfoLookup = Lookups.lookup(mixinInfo);
	private static final MethodHandles.Lookup mixinStateLookup = Lookups.lookup(mixinState);

	private static final MethodHandle getMixins = Lookups.findGetterIn(
		targetClassContextLookup,
		"mixins",
		SortedSet.class
	);
	private static final MethodHandle getState = Lookups.findGetterIn(mixinInfoLookup, "state", mixinState);
	private static final MethodHandle getClassNode = Lookups.findGetterIn(
		mixinStateLookup,
		"classNode",
		ClassNode.class
	);
	private static final MethodHandle getClassInfo = Lookups.findGetterIn(
		mixinStateLookup,
		"classInfo",
		ClassInfo.class
	);

	public static boolean isInternalTargetClassData(final ITargetClassContext context) {
		return targetClassContext.isInstance(context);
	}

	@SuppressWarnings("unchecked")
	public static SortedSet<? extends IMixinInfo> getMixins(ITargetClassContext context) {
		try {
			return (SortedSet<? extends IMixinInfo>) getMixins.invoke(context);
		} catch (Throwable t) {
			throw new AssertionError(t);
		}
	}

	public static Object getState(IMixinInfo mixinInfo) {
		try {
			return getState.invoke(mixinInfo);
		} catch (Throwable t) {
			throw new AssertionError(t);
		}
	}

	public static ClassNode getClassNode(Object state) {
		try {
			return (ClassNode) getClassNode.invoke(state);
		} catch (Throwable t) {
			throw new AssertionError(t);
		}
	}

	public static ClassInfo getClassInfo(Object state) {
		try {
			return (ClassInfo) getClassInfo.invoke(state);
		} catch (Throwable t) {
			throw new AssertionError(t);
		}
	}

	//endregion

	public static ILogger logger() {
		return MixinService.getService().getLogger(walker.getCallerClass().getName());
	}

	public static List<Type> getTargets(final ClassNode mixin) {
		final List<Type> types = new ArrayList<>();

		for (final var node : Annotations.findAnnotations(mixin, "Lorg/spongepowered/asm/mixin/Mixin;")) {
			CollectionUtils.checkedCopy(types, Annotations.find(node, "value", List.class), Type.class);
			CollectionUtils.checkedForEach(Annotations.find(node, "targets", List.class), String.class, value -> {
				types.add(Type.getObjectType(value));
			});
		}

		return types;
	}

	// region @Desc to target string
	public static List<String> descToTarget(final AnnotationNode desc) {
		if (!"Lorg/spongepowered/asm/mixin/injection/Desc;".equals(desc.desc)) {
			throw new IllegalArgumentException("Not a Desc: " + desc.desc);
		}

		final List<String> targets = new ArrayList<>();

		final Map<String, ?> map = Annotations.toMap(desc);

		// TODO: validate whether this is actually necessary?
		//  We probably won't use this but, hey?
		// For now, neglect polyfilling in.
		final Type owner = Type.VOID_TYPE; //CollectionUtils.checkedGetElse(map, "owner", Type.class, Type.VOID_TYPE);

		final String target = CollectionUtils.checkedGetElse(map, "value", String.class, "");
		final List<?> args = CollectionUtils.checkedGetElse(map, "args", List.class, List.of());
		final Type ret = CollectionUtils.checkedGetElse(map, "ret", Type.class, Type.VOID_TYPE);
		final List<?> next = CollectionUtils.checkedGetElse(map, "next", List.class, List.of());

		targets.add(descToTarget(owner, target, args, ret));

		for (final var value : next) {
			if (!(value instanceof Map<?, ?> nextMap)) {
				throw new IllegalArgumentException("Not a map: " + value);
			}

			final String nextTarget = CollectionUtils.checkedGetElse(nextMap, "name", String.class, "");
			final List<?> nextArgs = CollectionUtils.checkedGetElse(nextMap, "args", List.class, List.of());
			final Type nextRet = CollectionUtils.checkedGetElse(nextMap, "ret", Type.class, Type.VOID_TYPE);

			targets.add(descToTarget(owner, nextTarget, nextArgs, nextRet));
		}

		return targets;
	}

	private static String descToTarget(final Type type, final String target, final List<?> args, final Type ret) {
		final StringBuilder builder = new StringBuilder();

		if (type != null && type != Type.VOID_TYPE) {
			builder.append(type.descriptor);
		}

		builder.append(target);

		builder.append('(');

		for (final var arg : args) {
			if (!(arg instanceof Type argType)) {
				throw new IllegalArgumentException("Not a Type: " + arg);
			}

			builder.append(argType.descriptor);
		}

		builder.append(')');

		builder.append(ret.descriptor);

		return builder.toString();
	}
	// endregion
}
