package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the arguments of a function reference.
 */
final class FunctionReferenceArgumentParser {

	/**
	 * The input string.
	 */
	private final String args;

	/**
	 * The list of unmapped arguments.
	 */
	private final List<Argument> arguments = new ArrayList<>();

	/**
	 * The char index.
	 */
	private int index = 0;

	/**
	 * Constructs a new function argument parser based on the
	 * input string and instantly calculates the result.
	 *
	 * @param args The input string.
	 */
	public FunctionReferenceArgumentParser(String args) {
		this.args = args;

		parse();
	}

	private char c;
	private boolean nameFound = false;
	private final StringBuilder namePart = new StringBuilder();
	private final StringBuilder exprPart = new StringBuilder();

	private boolean inString = false;
	private int nesting = 0;

	private void parse() {
		while (index < args.length()) {
			c = args.charAt(index);

			// first try to compile the name
			if (!nameFound) {
				c = args.charAt(index);

				if (c == '_' || Character.isLetterOrDigit(c)) {
					namePart.append(c);
					exprPart.append(c);
					index++;
					continue;
				}

				// then if we have a name, start parsing the second part
				if (nesting == 0 && c == ':' && !namePart.isEmpty()) {
					exprPart.setLength(0);
					index++;
					nameFound = true;
					continue;
				}

				if (handleSpecialCharacters(Type.UNNAMED)) continue;

				namePart.setLength(0);
				nextExpr();
				continue;
			}

			if (handleSpecialCharacters(Type.NAMED)) continue;

			nextExpr();
		}

		if (args.isEmpty()) {
			return;
		}

		if (nameFound) {
			save(Type.NAMED);
		} else {
			save(Type.UNNAMED);
		}
	}

	private boolean handleSpecialCharacters(Type type) {
		// for strings
		if (!inString && c == '"') {
			nesting++;
			inString = true;
			nextExpr();
			return true;
		}

		if (inString && c == '"') {
			nesting--;
			inString = false;
			nextExpr();
			return true;
		}

		if (c == '(' || c == '{') {
			nesting++;
			nextExpr();
			return true;
		}

		if (c == ')' || c == '}') {
			nesting--;
			nextExpr();
			return true;
		}

		if (nesting == 0 && c == ',') {
			save(type);
			return true;
		}

		return false;
	}

	private void save(Type type) {
		if (type == Type.UNNAMED) {
			arguments.add(new Argument(Type.UNNAMED, null, exprPart.toString().trim()));
		} else {
			arguments.add(new Argument(Type.NAMED, namePart.toString().trim(), exprPart.toString().trim()));
		}

		namePart.setLength(0);
		exprPart.setLength(0);
		index++;
		nameFound = false;
	}

	private void nextExpr() {
		exprPart.append(c);
		index++;
	}

	/**
	 * Returns all arguments.
	 *
	 * @return All arguments.
	 */
	public Argument[] getArguments() {
		return arguments.toArray(new Argument[0]);
	}

	/**
	 * An argument.
	 *
	 * @param type  The type of the argument.
	 * @param name  The name of the argument, possibly null.
	 * @param value The value of the argument.
	 */
	public record Argument(
		Type type,
		@Nullable String name,
		String value
	) { }

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
