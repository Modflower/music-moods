package gay.ampflower.musicmoods.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyExpressionValueNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableWrapOperationNode;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * @author Ampflower
 * @since 0.7
 **/
// What some might consider, mixin cubed
public final class FrozenLibMusicManagerAdjuster implements MixinClassAdjuster {
	private static final ILogger logger = MixinSupport.logger();

	private static final String musicName = "gay/ampflower/musicmoods/client/sound/MusicSoundInstance";
	private static final String musicDesc = "L" + musicName + ";";

	private final Map<String, BiPredicate<MethodNode, Renamer>> sourceAdjusters = new HashMap<>();

	{
		sourceAdjusters.put("net.frozenblock.lib.music.mixin.client.MusicManagerMixin", (method, renamer) -> {
			if (!method.name.equals("frozenLib$startPlayingAtCorrectPitch")) {
				return false;
			}

			final var itr = method.visibleAnnotations.listIterator();
			while (itr.hasNext()) {
				final var annotation = AdjustableAnnotationNode.fromNode(itr.next());

				if (annotation.is(Taint.class)) {
					final var raw = annotation.get("session");
					if (raw.isPresent && raw.get().equals(Taint.session)) {
						break;
					}
				}

				// Ideally, we'd also check the descriptor directly, but that's not, trivial.
				if (annotation.is(ModifyExpressionValue.class)) {
					applyFrozenModifyExprPatch(itr, method, renamer, annotation);
					return true;
				}

				if (annotation.is(WrapOperation.class)) {
					applyFrozenWrapOperPatch(itr, method, renamer, annotation);
					return true;
				}
			}

			return false;
		});
	}

	private static void applyFrozenWrapOperPatch(
		final ListIterator<AnnotationNode> itr,
		final MethodNode method,
		final Renamer renamer,
		final AdjustableAnnotationNode annotation
	) {
		final var anno = annotation.as(AdjustableWrapOperationNode.class);

		anno.getMethod().set(0, "@MixinSquared:Handler");

		itr.set(anno);

		itr.add(SquaredSupport.targetHandler(
			"gay.ampflower.musicmoods.mixin.MixinMusicManager",
			"startPlayingCommon(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/network/chat/Component;FF)V"
		));
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
