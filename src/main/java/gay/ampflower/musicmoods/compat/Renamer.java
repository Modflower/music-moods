package gay.ampflower.musicmoods.compat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

/**
 * @author Ampflower
 * @since 0.7
 **/
public sealed interface Renamer {
	void setName(final String value);

	void setDesc(final String value);

	static Field ofField(ClassInfo classInfo, FieldNode node) {
		final var info = classInfo.findField(node);

		if (info == null) {
			throw new AssertionError(classInfo + " => 0x" + Integer.toHexString(node.access) + " " + node.name + "; " + node.desc);
		}

		return new Field(node, info);
	}

	static Field ofField(ClassNode classNode, ClassInfo classInfo, String name, String desc, int flags) {
		final var info = classInfo.findField(name, desc, flags);

		if (info == null) {
			throw new AssertionError(classInfo + " => 0x" + Integer.toHexString(flags) + " " + name + "; " + desc);
		}

		FieldNode node = null;

		for (final var field : classNode.fields) {
			if (!name.equals(field.name)) {
				continue;
			}
			if (!desc.equals(field.desc)) {
				continue;
			}
			if ((flags & ClassInfo.INCLUDE_STATIC) == 0 && (field.access & Opcodes.ACC_STATIC) != 0) {
				continue;
			}
			if ((flags & ClassInfo.INCLUDE_PRIVATE) == 0 && (field.access & Opcodes.ACC_PRIVATE) != 0) {
				continue;
			}
			node = field;
			break;
		}

		if (node == null) {
			throw new AssertionError(classNode + " => 0x" + Integer.toHexString(flags) + " " + name + "; " + desc);
		}

		return new Field(node, info);
	}

	static Method ofMethod(ClassInfo classInfo, MethodNode node) {
		final boolean isInit = "<init>".equals(node.name) || "<clinit>".equals(node.name);

		final var info = classInfo.findMethod(node, node.access | (isInit ? ClassInfo.INCLUDE_INITIALISERS : 0));

		if (info == null) {
			throw new AssertionError(classInfo + " => 0x" + Integer.toHexString(node.access) + " " + node.name + "; " + node.desc);
		}

		return new Method(node, info);
	}

	static Method ofMethod(ClassNode classNode, ClassInfo classInfo, String name, String desc, int flags) {
		final var info = classInfo.findMethod(name, desc, flags);

		if (info == null) {
			throw new AssertionError(classInfo + " => 0x" + Integer.toHexString(flags) + " " + name + "; " + desc);
		}

		MethodNode node = null;

		for (final var method : classNode.methods) {
			if (!name.equals(method.name)) {
				continue;
			}
			if (!desc.equals(method.desc)) {
				continue;
			}
			if ((flags & ClassInfo.INCLUDE_STATIC) == 0 && (method.access & Opcodes.ACC_STATIC) != 0) {
				continue;
			}
			if ((flags & ClassInfo.INCLUDE_PRIVATE) == 0 && (method.access & Opcodes.ACC_PRIVATE) != 0) {
				continue;
			}
			node = method;
			break;
		}

		if (node == null) {
			throw new AssertionError(classNode + " => 0x" + Integer.toHexString(flags) + " " + name + "; " + desc);
		}

		return new Method(node, info);
	}

	record Field(FieldNode node, ClassInfo.Field info) implements Renamer {
		@Override
		public void setName(final String value) {
			this.node.name = value;
			this.info.renameTo(value);
		}

		@Override
		public void setDesc(final String value) {
			this.node.desc = value;
			this.info.remapTo(value);
		}
	}

	record Method(MethodNode node, ClassInfo.Method info) implements Renamer {
		@Override
		public void setName(final String value) {
			this.node.name = value;
			this.info.renameTo(value);
		}

		@Override
		public void setDesc(final String value) {
			this.node.desc = value;
			this.info.remapTo(value);
		}
	}
}
