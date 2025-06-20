package ch.njol.skript.lang;

import ch.njol.skript.lang.FunctionReferenceArgumentParser.Argument;
import ch.njol.skript.lang.FunctionReferenceArgumentParser.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FunctionArgumentParserTest {

	@Test
	public void testUnnamedArgs() {
		Argument[] arguments = new FunctionReferenceArgumentParser("1, 2, \"hey:, gi:rl\", ({forza, real::*}, {_x::2}, 2)").getArguments();

		assertEquals(new Argument(Type.UNNAMED, null, "1"), arguments[0]);
		assertEquals(new Argument(Type.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument(Type.UNNAMED, null, "\"hey:, gi:rl\""), arguments[2]);
		assertEquals(new Argument(Type.UNNAMED, null, "({forza, real::*}, {_x::2}, 2)"), arguments[3]);

		arguments = new FunctionReferenceArgumentParser("1, 2, \"hey, girl\", ({forza, real}, 2)").getArguments();

		assertEquals(new Argument(Type.UNNAMED, null, "1"), arguments[0]);
		assertEquals(new Argument(Type.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument(Type.UNNAMED, null, "\"hey, girl\""), arguments[2]);
		assertEquals(new Argument(Type.UNNAMED, null, "({forza, real}, 2)"), arguments[3]);
	}

	@Test
	public void testNamedArgs() {
		Argument[] arguments = new FunctionReferenceArgumentParser("a_rg: 1, 2, womp: \"hey:, gi:rl\", list: ({forza, real::*}, {_x::2}, 2)").getArguments();

		assertEquals(new Argument(Type.NAMED, "a_rg", "1"), arguments[0]);
		assertEquals(new Argument(Type.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument(Type.NAMED, "womp", "\"hey:, gi:rl\""), arguments[2]);
		assertEquals(new Argument(Type.NAMED, "list", "({forza, real::*}, {_x::2}, 2)"), arguments[3]);

		arguments = new FunctionReferenceArgumentParser("2: 1, 2, 3_60: \"hey, girl\", 1list: ({forza, real}, 2)").getArguments();

		assertEquals(new Argument(Type.NAMED, "2", "1"), arguments[0]);
		assertEquals(new Argument(Type.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument(Type.NAMED, "3_60", "\"hey, girl\""), arguments[2]);
		assertEquals(new Argument(Type.NAMED, "1list", "({forza, real}, 2)"), arguments[3]);
	}

}
