package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ScriptFunction<T> extends Function<T> implements ReturnHandler<T> {

	private final Trigger trigger;

	private boolean returnValueSet;
	private T @Nullable [] returnValues;

	public ScriptFunction(Signature<T> sign, SectionNode node) {
		super(sign);

		Functions.currentFunction = this;
		try {
			trigger = loadReturnableTrigger(node, "function " + sign.getName(), new SimpleEvent());
		} finally {
			Functions.currentFunction = null;
		}
		trigger.setLineNumber(node.getLine());
	}

	// REMIND track possible types of local variables (including undefined variables) (consider functions, commands, and EffChange) - maybe make a general interface for this purpose
	// REM: use patterns, e.g. {_a%b%} is like "a.*", and thus subsequent {_axyz} may be set and of that type.
	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		Parameter<?>[] parameters = getSignature().getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> parameter = parameters[i];
			Object[] val = params[i];
			if (parameter.single && val.length > 0) {
				Variables.setVariable(parameter.name, val[0], event, true);
			} else {
				for (int j = 0; j < val.length; j++) {
					Variables.setVariable(parameter.name + "::" + (j + 1), val[j], event, true);
				}
			}
		}
		
		trigger.execute(event);
		ClassInfo<T> returnType = getReturnType();
		return returnType != null ? returnValues : null;
	}

	@Override
	public T execute(Event event, FunctionArguments arguments) {
		FunctionEvent<T> e = new FunctionEvent<>(this);

		for (String name : arguments.getNames()) {
			Variables.setVariable(name, arguments.get(name), e, true);
		}

		trigger.execute(e);
		ClassInfo<T> returnType = getReturnType();

		if (returnType == null || returnValues == null) {
			return null;
		}

		if (isSingle()) {
			if (returnValues.length == 0) {
				return null;
			}
			return returnValues[0];
		} else {
			//noinspection unchecked
			return (T) returnValues;
		}
	}

	@Override
	public boolean resetReturnValue() {
		returnValueSet = false;
		returnValues = null;
		return true;
	}

	@Override
	public final void returnValues(Event event, Expression<? extends T> value) {
		assert !returnValueSet;
		returnValueSet = true;
		this.returnValues = value.getArray(event);
	}

	@Override
	public final boolean isSingleReturnValue() {
		return isSingle();
	}

	@Override
	public final @Nullable Class<? extends T> returnValueType() {
		return getReturnType() != null ? getReturnType().getC() : null;
	}

}
