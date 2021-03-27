package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class ExpectedInputTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testEnums() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(InputType.Enum);
		exp.addEnum("eA");
		exp.addEnum("eB");
		exp.addEnum("eC");

		assertTrue(exp.validate(new Intent("", 0), "eA"));
		assertTrue(exp.validate(new Intent("", 0),"eC"));
		assertFalse(exp.validate(new Intent("", 0),"hallo"));
		assertFalse(exp.validate(new Intent("", 0),"je342se"));

	}

	
	@Test
	public void testInputNumber() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(InputType.Number);

		assertTrue(exp.validate("123"));
		assertTrue(exp.validate("9"));
		assertFalse(exp.validate("hallo"));
		assertFalse(exp.validate("je342se"));

	}

	@Test
	public void testInputWord() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(InputType.Word);

		assertTrue(exp.validate("asdgegd"));
		assertTrue(exp.validate("Gersdgolj"));
		assertFalse(exp.validate("hallo aseg geag eg"));
		assertFalse(exp.validate("Afgeg je342se"));

	}

	@Test
	public void testInputUrl() {

		ExpectedInput exp = new ExpectedInput();
		exp.setType(InputType.Url);

		assertTrue(exp.validate("https://hellotest.com"));
		assertTrue(exp.validate("http://aisgheig"));
		assertFalse(exp.validate("http://  halloasdg"));
		assertFalse(exp.validate("asdghesg"));

	}

}
