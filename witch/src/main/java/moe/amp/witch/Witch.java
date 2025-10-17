package moe.amp.witch;

import java.lang.instrument.Instrumentation;

/**
 * @author Ampflower
 **/
public final class Witch {

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		System.err.println("Compiler witch has been loaded. Ignoring arguments of \"" + agentArgs + "\" for as we don't need them here.");
		instrumentation.addTransformer(new Spell());
	}
}
