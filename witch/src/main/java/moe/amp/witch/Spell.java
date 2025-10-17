package moe.amp.witch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Ampflower
 **/
public final class Spell implements ClassFileTransformer {
	@Override
	public byte[] transform(
		final ClassLoader loader,
		final String className,
		final Class<?> classBeingRedefined,
		final ProtectionDomain protectionDomain,
		final byte[] classfileBuffer
	) throws IllegalClassFormatException {
		if (!"manifold/preprocessor/definitions/Definitions".equals(className)) {
			return null;
		}

		final ClassNode node = new ClassNode();

		new ClassReader(classfileBuffer).accept(node, 0);

		for (final MethodNode method : node.methods) {
			if (!"findBuildProperties".equals(method.name)) {
				continue;
			}
			if (!"(Lmanifold/api/fs/IResource;)Lmanifold/preprocessor/definitions/Definitions;".equals(method.desc)) {
				continue;
			}
			method.instructions.clear();
			method.visitInsn(Opcodes.ACONST_NULL);
			method.visitInsn(Opcodes.ARETURN);
			method.visitMaxs(1, 2);
			break;
		}

		final ClassWriter writer = new ClassWriter(0);
		node.accept(writer);

		return writer.toByteArray();
	}
}
