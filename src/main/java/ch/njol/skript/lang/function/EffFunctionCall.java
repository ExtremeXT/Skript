package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class EffFunctionCall extends Effect {
	
	private final org.skriptlang.skript.lang.function.FunctionReference<?> function;
	
	public EffFunctionCall(org.skriptlang.skript.lang.function.FunctionReference<?> function) {
		this.function = function;
	}
	
	@Nullable
	public static EffFunctionCall parse(final String line) {
		org.skriptlang.skript.lang.function.FunctionReference<?> function = new SkriptParser(line, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseFunction();
		if (function != null)
			return new EffFunctionCall(function);
		return null;
	}
	
	@Override
	protected void execute(final Event event) {
		function.execute();
		function.function().resetReturnValue(); // Function might have return value that we're ignoring
	}
	
	@Override
	public String toString(@Nullable final Event event, final boolean debug) {
		return function.function().toString();
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		assert false;
		return false;
	}
	
}
