package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

public class ExprFunctionCall<T> extends SimpleExpression<T> {

	private final org.skriptlang.skript.lang.function.FunctionReference<?> function;
	private final Class<? extends T>[] returnTypes;
	private final Class<T> returnType;

	@SuppressWarnings("unchecked")
	public ExprFunctionCall(org.skriptlang.skript.lang.function.FunctionReference<?> function, Class<? extends T>[] expectedReturnTypes) {
		this.function = function;
		Class<?> functionReturnType = function.signature().getReturnType().getC();
		assert  functionReturnType != null;
		if (CollectionUtils.containsSuperclass(expectedReturnTypes, functionReturnType)) {
			// Function returns expected type already
			this.returnTypes = new Class[] {functionReturnType};
			this.returnType = (Class<T>) functionReturnType;
		} else {
			// Return value needs to be converted
			this.returnTypes = expectedReturnTypes;
			this.returnType = (Class<T>) Utils.getSuperType(expectedReturnTypes);
		}
	}

	@Override
	protected T @Nullable [] get(Event event) {
		Object returnValue = function.execute(event);
		function.function().resetReturnValue();

		if (isSingle()) {
			return Converters.convert(new Object[] { returnValue }, returnTypes, returnType);
		} else {
			return Converters.convert((Object[]) returnValue, returnTypes, returnType);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;
		assert function.signature().getReturnType() != null;
		if (Converters.converterExists(function.signature().getReturnType().getC(), to)) {
			return new ExprFunctionCall<>(function, to);
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return function.function().isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return function.toString();
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		assert false;
		return false;
	}

}
