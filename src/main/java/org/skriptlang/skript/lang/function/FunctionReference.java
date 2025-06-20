package org.skriptlang.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.*;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

public final class FunctionReference<T> {

	private final String namespace;
	private final String name;
	private final Argument<Expression<?>>[] arguments;

	private Function<? extends T> cachedFunction;
	private LinkedHashMap<String, Expression<?>> cachedArguments;

	public FunctionReference(String namespace, String name, Argument<Expression<?>>[] arguments) {
		this.namespace = namespace;
		this.name = name;
		this.arguments = arguments;
	}

	public boolean validate() {
		if (cachedFunction == null) {
			//noinspection unchecked
			cachedFunction = (Function<? extends T>) Functions.getFunction(name, namespace);

			if (cachedFunction == null) {
				Skript.error("Function '%s' not found.".formatted(name));
				return false;
			}
		}

		if (cachedArguments == null) {
			cachedArguments = new LinkedHashMap<>();

			// get the target params of the function
			LinkedHashMap<String, Parameter<?>> targetParameters = new LinkedHashMap<>();
			for (Parameter<?> parameter : cachedFunction.getParameters()) {
				targetParameters.put(parameter.getName(), parameter);
			}

			for (Argument<Expression<?>> argument : arguments) {
				Parameter<?> target;
				if (argument.type == Type.NAMED) {
					target = targetParameters.get(argument.name);
				} else {
					target = targetParameters.firstEntry().getValue();
				}

				// try to parse value in the argument
				//noinspection unchecked
				Expression<?> converted = argument.value.getConvertedExpression(target.getType().getC());

				// failed to parse value
				if (converted == null) {
					if (LiteralUtils.hasUnparsedLiteral(argument.value)) {
						Skript.error("Can't understand this expression: %s".formatted(argument.value));
					} else {
						Skript.error("Type mismatch for argument '%s' in function '%s'. Expected: %s, got %s."
							.formatted(target.getName(), name, argument.value.getReturnType(), target.getType()));
					}
					return false;
				}

				// all good
				cachedArguments.put(target.getName(), converted);
				targetParameters.remove(target.getName());
			}
		}

		return true;
	}

	public T execute(Event event) {
		if (!validate()) {
			Skript.error("Epic function fail");
			return null;
		}

		LinkedHashMap<String, Object> args = new LinkedHashMap<>();
		cachedArguments.forEach((k, v) -> args.put(k, v.getSingle(event)));

		return cachedFunction.execute(new FunctionEvent<>(cachedFunction), new FunctionArguments(args));
	}

	public Function<? extends T> function() {
		if (cachedFunction == null) {
			//noinspection unchecked
			cachedFunction = (Function<? extends T>) Functions.getFunction(name, namespace);
		}

		return cachedFunction;
	}

	public String namespace() {
		return namespace;
	}

	public String name() {
		return name;
	}

	public Argument<Expression<?>>[] arguments() {
		return arguments;
	}

	/**
	 * An argument.
	 *
	 * @param type  The type of the argument.
	 * @param name  The name of the argument, possibly null.
	 * @param value The value of the argument.
	 */
	public record Argument<T>(
		Type type,
		@Nullable String name,
		T value
	) {

	}

	/**
	 * The type of argument.
	 */
	public enum Type {
		/**
		 * Whether this argument has a name.
		 */
		NAMED,

		/**
		 * Whether this argument does not have a name.
		 */
		UNNAMED
	}

}
