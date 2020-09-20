package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;

public class ExpectedInputTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testInputNumber() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(ExpectedInputType.Number);

		assertTrue(exp.validate("123"));
		assertTrue(exp.validate("9"));
		assertFalse(exp.validate("hallo"));
		assertFalse(exp.validate("je342se"));

	}

	@Test
	public void testInputWord() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(ExpectedInputType.Word);

		assertTrue(exp.validate("asdgegd"));
		assertTrue(exp.validate("Gersdgolj"));
		assertFalse(exp.validate("hallo aseg geag eg"));
		assertFalse(exp.validate("Afgeg je342se"));

	}

	@Test
	public void testInputUrl() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(ExpectedInputType.Url);

		assertTrue(exp.validate("https://hellotest.com"));
		assertTrue(exp.validate("http://aisgheig"));
		assertFalse(exp.validate("http://  halloasdg"));
		assertFalse(exp.validate("asdghesg"));

	}

}
