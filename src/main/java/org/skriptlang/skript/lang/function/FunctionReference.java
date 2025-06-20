package org.skriptlang.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.*;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

public final class FunctionReference<T> {

	private final String namespace;
	private final String name;
	private final Argument<Expression<?>>[] arguments;

	private Function<? extends T> cachedFunction;
	private FunctionArguments cachedArguments;

	public FunctionReference(String namespace, String name, Argument<Expression<?>>[] arguments) {
		this.namespace = namespace;
		this.name = name;
		this.arguments = arguments;
	}

	public T execute() {
		if (cachedFunction == null) {
			//noinspection unchecked
			cachedFunction = (Function<? extends T>) Functions.getFunction(name, namespace);
		}

		if (cachedFunction == null) {
			Skript.error("Function '%s' not found.".formatted(name));
			return null;
		}

		if (cachedArguments == null) {
			LinkedHashMap<String, Object> args = new LinkedHashMap<>();

			LinkedHashMap<String, Parameter<?>> parameters = new LinkedHashMap<>();
			for (Parameter<?> parameter : cachedFunction.getParameters()) {
				parameters.put(parameter.getName(), parameter);
			}

			for (Argument<Expression<?>> argument : arguments) {
				if (argument.type == Type.NAMED) {
					args.put(argument.name, argument.value);
					parameters.remove(argument.name);
				} else {
					// get the first available parameter
					Parameter<?> parameter = parameters.firstEntry().getValue();

					args.put(parameter.getName(), argument.value);
					parameters.remove(parameter.getName());
				}
			}

			cachedArguments = new FunctionArguments(args);
		}

		return cachedFunction.execute(new FunctionEvent<>(cachedFunction), cachedArguments);
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
