package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultFunction<T> extends JavaFunction<T> {

    private final Function<FunctionArguments, T> execute;

    static {
        DefaultFunction<Double> build = builder("product", Double.class)
                .parameter("xs", Number[].class)
                .parameter("start", Number.class)
                .build(args -> {
                    Number[] xs = args.get("xs");

                    double product = args.getOrDefault("start", 1.0);
                    for (Number x : xs) {
                        product *= x.doubleValue();
                    }

                    return product;
                });
    }

    private DefaultFunction(
            String name, Parameter<?>[] parameters,
            ClassInfo<T> returnType, boolean single,
            @Nullable Contract contract, Function<FunctionArguments, T> execute
    ) {
        super(name, parameters, returnType, single, contract);

        this.execute = execute;
    }

    public static <T> Builder<T> builder(String name, Class<T> returnType) {
        return new Builder<>(name, returnType);
    }

    @Override
    public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
        throw new IllegalStateException("DefaultFunction should not call execute(FunctionEvent, Object[][])");
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
                    returnType.isArray(), contract, execute);
        }
    }

}
