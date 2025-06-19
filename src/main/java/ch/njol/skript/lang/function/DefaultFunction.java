package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class DefaultFunction<T> extends ch.njol.skript.lang.function.Function<T> {

	public static <T> Builder<T> builder(String name, Class<T> returnType) {
		return new Builder<>(name, returnType);
	}

	private String[] description;
	private String[] since;
	private String[] examples;

	private final Function<FunctionArguments, T> execute;

	public DefaultFunction(
		String name, Parameter<?>[] parameters,
		ClassInfo<T> returnType, boolean single,
		@Nullable Contract contract, Function<FunctionArguments, T> execute
	) {
		super(new Signature<>("none", name, parameters, false,
			returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));

		this.execute = execute;
	}

	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		throw new IllegalStateException("DefaultFunction should not call execute(FunctionEvent, Object[][])");
	}

	@Override
	public @Nullable T execute(FunctionEvent<?> event, FunctionArguments arguments) {
		return execute.apply(arguments);
	}

	@Override
	public boolean resetReturnValue() {
		return true;
	}

	public DefaultFunction<T> description(String... description) {
		this.description = description;
		return this;
	}

	public DefaultFunction<T> since(String... since) {
		this.since = since;
		return this;
	}

	public DefaultFunction<T> examples(String... examples) {
		this.examples = examples;
		return this;
	}

	public static class Builder<T> {

		private final String name;
		private final Class<T> returnType;
		private final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();

		private Contract contract = null;

		public Builder(String name, Class<T> returnType) {
			this.name = name;
			this.returnType = returnType;
		}

		public Builder<T> contract(Contract contract) {
			this.contract = contract;
			return this;
		}

		public <PT> Builder<T> parameter(String name, Class<PT> type) {
			ClassInfo<PT> classInfo = Classes.getExactClassInfo(type);
			if (classInfo == null) {
				throw new IllegalArgumentException("No ClassInfo found for " + type.getSimpleName());
			}
			parameters.put(name, new Parameter<>(name, classInfo, true, null));
			return this;
		}

		public DefaultFunction<T> build(Function<FunctionArguments, T> execute) {
			ClassInfo<T> classInfo = Classes.getExactClassInfo(returnType);

			if (classInfo == null) {
				throw new IllegalArgumentException("No ClassInfo found for " + returnType.getSimpleName());
			}

			return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), classInfo,
				!returnType.isArray(), contract, execute);
		}
	}

}
