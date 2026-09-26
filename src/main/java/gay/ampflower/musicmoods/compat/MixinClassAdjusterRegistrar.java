package gay.ampflower.musicmoods.compat;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.transformers.MixinClassWriter;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ampflower
 * @since 0.7
 **/
public class MixinClassAdjusterRegistrar implements IExtension {
	private static final ILogger logger = MixinSupport.logger();

	private static final Map<String, List<MixinClassAdjuster>> registrar = new HashMap<>();

	public static void registerByClass(String targetClassName, MixinClassAdjuster action) {
		registrar.computeIfAbsent(targetClassName, $ -> new ArrayList<>()).add(action);

		logger.info("Registered {} to {}", action, targetClassName);
	}

	public static void registerByMixin(String mixinClassName, MixinClassAdjuster action) {
		final ClassNode mixinScaffold = Annotations.loadScaffolding(mixinClassName);
		final List<Type> targets = MixinSupport.getTargets(mixinScaffold);

		logger.info("Found {} targets bound to {}", targets.size(), mixinClassName);

		for (final Type type : targets) {
			registerByClass(type.className, action);
		}
	}

	@Override
	public boolean checkActive(final MixinEnvironment environment) {
		return !registrar.isEmpty;
	}

	@Override
	public void preApply(final ITargetClassContext context) {
		final List<MixinClassAdjuster> adjusters = registrar.get(context.classInfo.className);

		if (adjusters == null) {
			return;
		}

		if (!MixinSupport.isInternalTargetClassData(context)) {
			return;
		}

		try {
			final var mixins = MixinSupport.getMixins(context);

			final var taints = new ArrayList<AnnotationNode>();

			for (final var mixin : mixins) {
				final var mixinState = MixinSupport.getState(mixin);
				final var mixinClassNode = MixinSupport.getClassNode(mixinState);
				final var mixinClassInfo = MixinSupport.getClassInfo(mixinState);
				final var appliedAdjusters = new ArrayList<Type>();

				logger.debug("{} => {}", context.classInfo.className, mixinClassInfo.className);

				boolean flag = false;

				for (final var adjuster : adjusters) {
					if (adjuster.apply(
						context.classInfo.className,
						context.classInfo,
						context.classNode,
						mixinClassInfo.className,
						mixinClassInfo,
						mixinClassNode
					)) {
						final Class<?> adjusterClass = adjuster.getClass();
						logger.debug(
							"Applied {} to {} => {}",
							adjusterClass,
							context.classInfo.className,
							mixinClassInfo.className
						);

						if (adjusterClass.getName().indexOf('/') < 0) {
							appliedAdjusters.add(Type.getType(adjusterClass));
						}

						flag = true;
					}
				}


				final var taint = TaintUtil.taintOfType(
					Taint.session,
					context.classInfo.className,
					mixinClassInfo.className,
					appliedAdjusters
				);
				TaintUtil.addTaint(mixinClassNode, taint);
				taints.add(taint);

				if (flag) {
					final var writer = new MixinClassWriter(0);
					mixinClassNode.accept(writer);
					final var file = Files.createTempFile(mixinClassInfo.className, ".class");
					logger.info("Dumped the class to: {}", file);
					Files.write(file, writer.toByteArray());
				}
			}

			TaintUtil.addTaint(context.classNode, taints);

		} catch (Throwable t) {
			throw new AssertionError(t);
		}
	}

	@Override
	public void postApply(final ITargetClassContext context) {

	}

	@Override
	public void export(final MixinEnvironment env, final String name, final boolean force, final ClassNode classNode) {

	}
}
