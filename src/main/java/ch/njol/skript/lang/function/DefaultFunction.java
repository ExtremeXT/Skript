package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function that has been implemented in Java, instead of in Skript.
 * <p>
 * An example implementation is stated below.
 * <pre><code>
 * Functions.register(DefaultFunction.builder("floor", Long.class)
 * 	.description("Rounds a number down.")
 * 	.examples("floor(2.34) = 2")
 * 	.since("3.0")
 * 	.parameter("n", Number.class)
 * 	.build(args -> {
 * 		Object value = args.get("n");
 *
 * 		if (value instanceof Long l)
 * 			return l;
 *
 * 		return Math2.floor(((Number) value).doubleValue());
 *    }));
 * </code></pre>
 * </p>
 *
 * @param <T> The return type.
 * @see #builder(String, Class)
 */
public final class DefaultFunction<T> extends ch.njol.skript.lang.function.Function<T> {

	/**
	 * Creates a new builder for a function.
	 *
	 * @param name       The name of the function.
	 * @param returnType The type of the function.
	 * @param <T>        The return type.
	 * @return The builder for a function.
	 */
	public static <T> Builder<T> builder(@NotNull String name, @NotNull Class<T> returnType) {
		return new Builder<>(name, returnType);
	}

	private final Function<FunctionArguments, T> execute;
	private final BiFunction<Event, FunctionArguments, T> execute2;

	private final String[] description;
	private final String[] since;
	private final String[] examples;

	private DefaultFunction(
		String name, Parameter<?>[] parameters,
		ClassInfo<T> returnType, boolean single,
		@Nullable Contract contract, Function<FunctionArguments, T> execute,
		String[] description, String[] since, String[] examples
	) {
		super(new Signature<>("none", name, parameters, false,
			returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));

		this.execute = execute;
		this.execute2 = null;
		this.description = description;
		this.since = since;
		this.examples = examples;
	}

	private DefaultFunction(
		String name, Parameter<?>[] parameters,
		ClassInfo<T> returnType, boolean single,
		@Nullable Contract contract, BiFunction<Event, FunctionArguments, T> execute2,
		String[] description, String[] since, String[] examples
	) {
		super(new Signature<>("none", name, parameters, false,
			returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));

		this.execute = null;
		this.execute2 = execute2;
		this.description = description;
		this.since = since;
		this.examples = examples;
	}

	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		throw new IllegalStateException("DefaultFunction should not call execute(FunctionEvent, Object[][])");
	}

	@Override
	public @Nullable T execute(Event event, FunctionArguments arguments) {
		if (execute == null) {
			return execute2.apply(event, arguments);
		}

		return execute.apply(arguments);
	}

	@Override
	public boolean resetReturnValue() {
		return true;
	}

	/**
	 * Returns this function's description.
	 *
	 * @return The description.
	 */
	public String @NotNull [] description() {
		return description;
	}

	/**
	 * Returns this function's version history.
	 *
	 * @return The version history.
	 */
	public String @NotNull [] since() {
		return since;
	}

	/**
	 * Returns this function's examples.
	 *
	 * @return The examples.
	 */
	public String @NotNull [] examples() {
		return examples;
	}

	public static class Builder<T> {

		private final String name;
		private final Class<T> returnType;
		private final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();

		private Contract contract = null;

		private String[] description;
		private String[] since;
		private String[] examples;

		private Builder(@NotNull String name, @NotNull Class<T> returnType) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(returnType, "return type cannot be null");

			this.name = name;
			this.returnType = returnType;
		}

		public Builder<T> contract(@NotNull Contract contract) {
			Preconditions.checkNotNull(contract, "contract cannot be null");

			this.contract = contract;
			return this;
		}

		/**
		 * Sets this function builder's description.
		 *
		 * @return This builder.
		 */
		public Builder<T> description(@NotNull String... description) {
			Preconditions.checkNotNull(description, "description cannot be null");

			this.description = description;
			return this;
		}

		/**
		 * Sets this function builder's version history.
		 *
		 * @return This builder.
		 */
		public Builder<T> since(@NotNull String... since) {
			Preconditions.checkNotNull(since, "since cannot be null");

			this.since = since;
			return this;
		}

		/**
		 * Sets this function builder's examples.
		 *
		 * @return This builder.
		 */
		public Builder<T> examples(@NotNull String... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");

			this.examples = examples;
			return this;
		}

		/**
		 * Adds a parameter to this function builder.
		 *
		 * @param name The parameter name.
		 * @param type The type of the parameter.
		 * @return This builder.
		 */
		public <PT> Builder<T> parameter(@NotNull String name, @NotNull Class<PT> type) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");

			ClassInfo<PT> classInfo = Classes.getExactClassInfo(type);
			if (classInfo == null) {
				throw new IllegalArgumentException("No type found for " + type.getSimpleName());
			}
			parameters.put(name, new Parameter<>(name, classInfo, !type.isArray(), null));
			return this;
		}

		public <PT> Builder<T> optionalParameter(@NotNull String name, @NotNull Class<PT> type) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");

			ClassInfo<PT> classInfo = Classes.getExactClassInfo(type);
			if (classInfo == null) {
				throw new IllegalArgumentException("No type found for " + type.getSimpleName());
			}
			parameters.put(name, new Parameter<>(name, classInfo, !type.isArray(), true));
			return this;
		}

		/**
		 * Completes this builder with the code to execute on call of this function.
		 *
		 * @param execute The code to execute.
		 * @return The final function.
		 */
		public DefaultFunction<T> build(Function<FunctionArguments, T> execute) {
			Preconditions.checkNotNull(execute, "execute cannot be null");
			ClassInfo<T> classInfo = Classes.getExactClassInfo(returnType);

			if (classInfo == null) {
				throw new IllegalArgumentException("No type found for " + returnType.getSimpleName());
			}

			return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), classInfo,
				!returnType.isArray(), contract, execute, description, since, examples);
		}

		/**
		 * Completes this builder with the code to execute on call of this function.
		 *
		 * @param execute The code to execute.
		 * @return The final function.
		 */
		public DefaultFunction<T> build(BiFunction<Event, FunctionArguments, T> execute) {
			Preconditions.checkNotNull(execute, "execute cannot be null");
			ClassInfo<T> classInfo = Classes.getExactClassInfo(returnType);

			if (classInfo == null) {
				throw new IllegalArgumentException("No type found for " + returnType.getSimpleName());
			}

			return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), classInfo,
				!returnType.isArray(), contract, execute, description, since, examples);
		}
	}

}
