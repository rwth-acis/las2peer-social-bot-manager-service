package i5.las2peer.services.socialBotManagerService.dialogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.TableLanguageGenerator;

public class TableLanguageGeneratorTest {

    @Test
    public void testAddEntry() {

	TableLanguageGenerator gen = new TableLanguageGenerator();
	gen.addEntry("ent1", "ent1.message");
	gen.addEntry("ent2", "ent2.message");
	assertNotNull(gen.getEntry("ent1"));
	assertNotNull(gen.getEntry("ent2"));
	assertEquals(1, gen.getEntry("ent1").size());
	assertEquals(1, gen.getEntry("ent2").size());
	assertEquals("ent1.message", gen.getEntry(("ent1")).get(0));
	assertEquals("ent2.message", gen.getEntry(("ent2")).get(0));

    }
    
    @Test
    public void testAddEntryMultiValues() {

	TableLanguageGenerator gen = new TableLanguageGenerator();
	gen.addEntry("ent1", "ent1.message1");
	gen.addEntry("ent1", "ent1.message2");
	gen.addEntry("ent2", "ent2.message1");
	gen.addEntry("ent1", "ent1.message3");
	assertNotNull(gen.getEntry("ent1"));
	assertNotNull(gen.getEntry("ent2"));
	assertEquals(3, gen.getEntry("ent1").size());
	assertEquals(1, gen.getEntry("ent2").size());
	assertTrue(gen.getEntry(("ent1")).contains("ent1.message1"));
	assertTrue(gen.getEntry(("ent1")).contains("ent1.message2"));
	assertTrue(gen.getEntry(("ent1")).contains("ent1.message3"));
	assertTrue(gen.getEntry(("ent2")).contains("ent2.message1"));
	assertFalse(gen.getEntry(("ent1")).contains("ent2.message1"));
	assertFalse(gen.getEntry(("ent2")).contains("ent1.message2"));

    }
    
    @Test
    public void testParse() {

	TableLanguageGenerator gen = new TableLanguageGenerator();
	gen.addEntry("in1", "in1.message");
	gen.addEntry("in2", "in2.message");
	gen.addEntry("in3", "in3.message");
	
	DialogueAct act = new DialogueAct();
	
	act.setIntent("in2");
	assertEquals("in2.message", gen.parse(act));
	act.setIntent("in1");
	assertEquals("in1.message", gen.parse(act));
	act.setIntent("in3");
	assertEquals("in3.message", gen.parse(act));
	act.setIntent("in2");
	assertEquals("in2.message", gen.parse(act));

    }
    
    @Test
    public void testParseWithEntity() {

	TableLanguageGenerator gen = new TableLanguageGenerator();
	gen.addEntry("in1", "in1.message with #en1 yes");
	
	DialogueAct act = new DialogueAct();	
	act.setIntent("in1");
	act.addEntity("en1", "hello");

	assertEquals("in1.message with hello yes", gen.parse(act));
	

    }

}
