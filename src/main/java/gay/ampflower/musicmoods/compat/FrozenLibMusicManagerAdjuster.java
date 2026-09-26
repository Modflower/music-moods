package gay.ampflower.musicmoods.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyExpressionValueNode;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * @author Ampflower
 * @since 0.7
 **/
// What some might consider, mixin cubed
public final class FrozenLibMusicManagerAdjuster implements MixinClassAdjuster {
	private static final ILogger logger = MixinSupport.logger();

	private static final String musicManager = "gay.ampflower.musicmoods.mixin.MixinMusicManager";
	private static final String startPlayingCommon;
	private static final String musicName = "gay/ampflower/musicmoods/client/sound/MusicSoundInstance";
	private static final String musicDesc = "L" + musicName + ";";

	private final Map<String, BiPredicate<MethodNode, Renamer>> sourceAdjusters = new HashMap<>();

	static {
		String common = null;
		final ClassNode node = Annotations.loadScaffolding(musicManager);

		for (final MethodNode method : node.methods) {
			for (final AnnotationNode annotation : Annotations.findAnnotations(
				method,
				Marker.class.descriptorString()
			)) {
				final String value = Annotations.find(annotation, "value", String.class);

				if ("frozenLibTarget".equals(value)) {
					common = method.name + method.desc;
				}
			}
		}

		startPlayingCommon = Objects.requireNonNull(common, "Unable to find frozenLibTarget");
	}

	{
		sourceAdjusters.put(
			"net.frozenblock.lib.music.mixin.client.MusicManagerMixin",
			(method, renamer) -> iterate(method, "frozenLib$startPlayingAtCorrectPitch", true, (itr, annotation) -> {
				// Ideally, we'd also check the descriptor directly, but that's not, trivial.
				if (annotation.is(ModifyExpressionValue.class)) {
					applyFrozenModifyExprPatch(itr, method, renamer, annotation);
					return true;
				}

				if (annotation.is(WrapOperation.class)) {
					applySimpleRewritePatch(itr, annotation, musicManager, startPlayingCommon);
					return true;
			}

			return false;
			})
		);

		sourceAdjusters.put(
			"com.blackgear.vanillabackport.core.mixin.client.music_toast.MusicManagerMixin",
			(method, renamer) -> iterate(method, "vb$updateVolume", true, (itr, annotation) -> {
				if (annotation.is(Inject.class)) {
					rewriteInjector(
						method,
						renamer,
						"(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
						null,
						null
					);
					applySimpleRewritePatch(itr, annotation, musicManager, startPlayingCommon);
					return true;
				}
				return false;
			})
		);

		sourceAdjusters.put(
			"com.blackgear.vanillabackport.core.mixin.client.MusicManagerMixin",
			(method, renamer) -> iterate(method, "updateVolume", true, (itr, annotation) -> {
				if (annotation.is(Inject.class)) {
					rewriteInjector(
						method,
						renamer,
						"(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
						null,
						null
					);
					applySimpleRewritePatch(itr, annotation, musicManager, startPlayingCommon);
					return true;
				}
				return false;
			})
		);
	}

	private static boolean iterate(
		final MethodNode method,
		final String expectedName,
		final boolean oneshot,
		final BiPredicate<ListIterator<AnnotationNode>, AdjustableAnnotationNode> consumer
	) {
		if (!method.name.equals(expectedName)) {
			return false;
		}

		boolean flag = false;

		final var itr = method.visibleAnnotations.listIterator();
		while (itr.hasNext() & !(oneshot & flag)) {
			final var annotation = AdjustableAnnotationNode.fromNode(itr.next());

			if (TaintUtil.test(annotation)) {
				break;
			}

			flag |= consumer.test(itr, annotation);
		}

		return flag;
	}

	// TODO: overhaul this in the polymorphic transformer
	private static void applySimpleRewritePatch(
		final ListIterator<AnnotationNode> itr,
		final AdjustableAnnotationNode annotation,
		final String mixin,
		final String target
	) {
		annotation.set("method", "@MixinSquared:Handler");
		itr.set(annotation);

		itr.add(SquaredSupport.targetHandler(mixin, target));
	}

	private static void rewriteInjector(
		final MethodNode method,
		final Renamer renamer,
		final String desiredDesc,
		final List<@NotNull AnnotationNode> @Nullable [] visible,
		final List<@NotNull AnnotationNode> @Nullable [] invisible,
		final int... originalParams
	) {
		final var originalParameters = Type.getArgumentTypes(method.desc);
		final var originalReturn = Type.getReturnType(method.desc);

		final var desiredParameters = Type.getArgumentTypes(desiredDesc);
		final var desiredReturn = Type.getReturnType(desiredDesc);

		if (originalReturn.getSort() != desiredReturn.getSort()) {
			throw new IllegalArgumentException("Incompatible return: Expected " + originalReturn.descriptor + ", got " + desiredReturn.descriptor);
		}

		// TODO: actually properly handle this; for the meantime, assert all the parameters are unused
		final boolean hasThis = (method.access & Opcodes.ACC_STATIC) == 0;
		final int slotGuard;

		{
			int count = hasThis ? 1 : 0;
			for (final var type : originalParameters) {
				count += type.size;
			}
			slotGuard = count;
		}

		for (final var insn : method.instructions) {
			if (insn instanceof VarInsnNode var && var.var < slotGuard && (!hasThis || var.var != 0)) {
				throw new AssertionError(String.format(
					"Slot %d is used by the target function %s%s",
					var.var,
					method.name,
					method.desc
				));
			}
		}

		renamer.setDesc(desiredDesc);
	}

	private static void applyFrozenModifyExprPatch(
		final ListIterator<AnnotationNode> itr,
		final MethodNode method,
		final Renamer renamer,
		final AdjustableAnnotationNode annotation
	) {
		final var anno = annotation.as(AdjustableModifyExpressionValueNode.class);
		final Type simpleSoundInstance = Type.getReturnType(method.desc);
		final String soundName = simpleSoundInstance.internalName;

		renamer.setDesc("(" + musicDesc + ")" + musicDesc);

		for (final AbstractInsnNode insn : method.instructions) {
			if (insn instanceof FieldInsnNode fieldInsn && fieldInsn.owner.equals(soundName)) {
				fieldInsn.owner = musicName;
			}
		}

		anno.getMethod().set(0, "@MixinSquared:Handler");

		anno.withAt(ats -> {
			ats[0].value = "NEW";
			ats[0].target = musicName;
			return ats;
		});

		itr.set(anno);

		itr.add(SquaredSupport.targetHandler(
			"gay.ampflower.musicmoods.mixin.MixinMusicManager",
			"startPlayingCommon(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/network/chat/Component;FF)V"
		));
	}

	@Override
	public boolean apply(
		final String targetClassName,
		final String mixinClassName,
		final ClassInfo mixinClassInfo,
		final ClassNode mixinClassNode
	) {
		final var action = sourceAdjusters.get(mixinClassName);
		if (action == null) {
			return false;
		}

		logger.info("Found {} -> {}", mixinClassName, action);

		boolean flag = false;
		for (final var method : mixinClassNode.methods) {
			if (action.test(method, Renamer.ofMethod(mixinClassInfo, method))) {
				TaintUtil.addTaint(method, TaintUtil.taintOfClass(
					Taint.session,
					targetClassName,
					mixinClassName,
					List.of(FrozenLibMusicManagerAdjuster.class)
				));
				flag = true;
			}
		}
		return flag;
	}
}
