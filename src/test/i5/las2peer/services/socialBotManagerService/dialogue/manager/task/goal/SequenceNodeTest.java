package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class SequenceNodeTest {

	Slot slotS;
	Slot slotA;
	Slot slotB;
	Slot parentSlot;
	Slot seqSlot;
	Slot multiSlot;
	ArrayList<String> enums;

	ServiceFunctionAttribute attrA;
	ServiceFunctionAttribute attrB;
	ServiceFunctionAttribute seqAttr;
	ServiceFunctionAttribute multiAttr;

	@Before
	public void setUp() {

		parentSlot = new Slot("parentSlot");
		slotS = new Slot("slotS");
		slotA = new Slot("slotA");
		slotB = new Slot("slotB");

		slotA.setInputType(InputType.Free);
		slotB.setInputType(InputType.Free);

		slotS.addChild(slotA);
		slotS.addChild(slotB);

		slotA.setRequired(true);
		slotB.setRequired(true);

		attrA = new ServiceFunctionAttribute("Ad", "An");
		attrB = new ServiceFunctionAttribute("Bd", "Bn");
		slotA.setParameter(attrA);
		slotB.setParameter(attrB);

		seqAttr = new ServiceFunctionAttribute("saId", "saName");
		multiAttr = new ServiceFunctionAttribute("maId", "maName");
		multiAttr.setArray(true);

		seqSlot = new Slot(seqAttr);
		multiSlot = new Slot(multiAttr);
		parentSlot.addChild(seqSlot);
		parentSlot.addChild(multiSlot);
		seqSlot.addChild(slotA);
		seqSlot.addChild(slotB);

	}

	@Test
	public void CreationTest() {

		SequenceNode node = new SequenceNode(slotS);

		assertNotNull(node.getChildren());
		assertEquals(2, node.getChildren().size());
		assertNotNull(node.getChildren().get(0));
		assertNotNull(node.getChildren().get(1));

	}

	@Test
	public void isReadyTest() {

		SequenceNode node = new SequenceNode(slotS);
		assertFalse(node.isReady());

		((Fillable) node.getChildren().get(0)).fill("At");
		assertFalse(node.isReady());

		((Fillable) node.getChildren().get(1)).fill("Bt");
		assertTrue(node.isReady());
		
	}

	@Test
	public void toJSONValueNodesTest() {

		SequenceNode node = new SequenceNode(slotS);
		assertNotNull(node.toBodyJSON());

		attrA.setParameterType(ParameterType.CHILD);
		attrB.setParameterType(ParameterType.CHILD);

		assertNotNull(node.toBodyJSON());
		assertEquals("{}", node.toBodyJSON().toString());

		((Fillable) node.getChildren().get(0)).fill("At");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"An\":\"At\"}", node.toBodyJSON().toString());

		((Fillable) node.getChildren().get(1)).fill("Bt");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"Bn\":\"Bt\",\"An\":\"At\"}", node.toBodyJSON().toString());

		attrA.setParameterType(ParameterType.PATH);
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"Bn\":\"Bt\"}", node.toBodyJSON().toString());
	}

	@Test
	public void toJSONMultiValueNodesTest() {

		Slot parentSlot = new Slot("", "parent", ParameterType.BODY);
		Slot multiSlotA = new Slot("", "multiA", ParameterType.CHILD);
		multiSlotA.setArray(true);
		Slot multiSlotB = new Slot("", "multiB", ParameterType.CHILD);
		multiSlotB.setArray(true);
		parentSlot.addChild(multiSlotA);
		parentSlot.addChild(multiSlotB);

		SequenceNode node = new SequenceNode(parentSlot);
		assertNotNull(node.toBodyJSON());

		assertNotNull(node.toBodyJSON());
		assertEquals("{}", node.toBodyJSON().toString());
		assertEquals(2, node.getChildren().size());

		((Fillable) node.getChildren().get(0)).fill("At1");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"multiA\":[\"At1\"]}", node.toBodyJSON().toString());

		((Fillable) node.getChildren().get(0)).fill("At2");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"multiA\":[\"At1\",\"At2\"]}", node.toBodyJSON().toString());

		((Fillable) node.getChildren().get(1)).fill("Bt");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"multiB\":[\"Bt\"],\"multiA\":[\"At1\",\"At2\"]}", node.toBodyJSON().toString());

		multiSlotA.getParameter().setParameterType(ParameterType.PATH);
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"multiB\":[\"Bt\"]}", node.toBodyJSON().toString());
	}

	@Test
	public void toJSONNestedTest() {

		Slot parentSlot = new Slot("parent", ParameterType.BODY);
		Slot seqSlotA = new Slot("seqA", ParameterType.CHILD);
		Slot seqSlotB = new Slot("seqB", ParameterType.CHILD);
		Slot valueSlotA = new Slot("valA", ParameterType.CHILD);
		Slot valueSlotB = new Slot("valB", ParameterType.CHILD);

		parentSlot.addChild(seqSlotA);
		parentSlot.addChild(seqSlotB);
		seqSlotA.addChild(valueSlotA);
		seqSlotB.addChild(valueSlotB);

		SequenceNode node = new SequenceNode(parentSlot);
		assertNotNull(node.toBodyJSON());

		assertNotNull(node.toBodyJSON());
		assertEquals(2, node.getChildren().size());

		((Fillable) ((SequenceNode) node.getChildren().get(0)).getChildren().get(0)).fill("At1");
		assertNotNull(node.toBodyJSON());
	
	}
	
	@Test
	public void toJSONMixedTest() {
		
		Slot parentSlot = new Slot("parent", ParameterType.BODY);
		Slot valueSlot = new Slot("value", ParameterType.CHILD);
		Slot multiSlot = new Slot("multiValue", ParameterType.CHILD);
		multiSlot.setArray(true);
		Slot seqSlot = new Slot("sequence", ParameterType.CHILD);	
		Slot seqValueSlot = new Slot("seqValue", ParameterType.CHILD);

		parentSlot.addChild(valueSlot);
		parentSlot.addChild(multiSlot);
		parentSlot.addChild(seqSlot);
		seqSlot.addChild(seqValueSlot);

		SequenceNode node = new SequenceNode(parentSlot);
		assertNotNull(node.toBodyJSON());

		assertNotNull(node.toBodyJSON());
		assertEquals(3, node.getChildren().size());

		((Fillable) ((SequenceNode) node.getChildren().get(2)).getChildren().get(0)).fill("Pika");
		assertNotNull(node.toBodyJSON());
		assertTrue(node.toBodyJSON().toString().contains("{\"seqValue\":\"Pika\"}"));
		
		((Fillable) node.getChildren().get(0)).fill("Bisa");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"value\":\"Bisa\",\"sequence\":{\"seqValue\":\"Pika\"}}", node.toBodyJSON().toString());
		
		((Fillable) node.getChildren().get(1)).fill("Gluma");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"multiValue\":[\"Gluma\"],\"value\":\"Bisa\",\"sequence\":{\"seqValue\":\"Pika\"}}", node.toBodyJSON().toString());
	
	}

}
