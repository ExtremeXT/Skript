package ch.njol.skript.lang.function;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class FunctionArguments {

	private final Map<String, Object> arguments;

	public FunctionArguments(LinkedHashMap<String, Object> arguments) {
		this.arguments = arguments;
	}

	public <T> T get(String name) {
		//noinspection unchecked
		return (T) arguments.get(name);
	}

	public <T> T getOrDefault(String name, T defaultValue) {
		//noinspection unchecked
		return (T) arguments.getOrDefault(name, defaultValue);
	}

	public boolean has(String name) {
		return arguments.containsKey(name);
	}

	Set<String> getNames() {
		return arguments.keySet();
	}

}
