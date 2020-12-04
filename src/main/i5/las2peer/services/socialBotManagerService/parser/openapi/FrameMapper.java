package i5.las2peer.services.socialBotManagerService.parser.openapi;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

/**
 * Handles the integration of service functions and frames.
 *
 */
public class FrameMapper {

	/**
	 * Creates a new frame consistent with a service function
	 * 
	 * @param serviceURL
	 * @param operationID
	 * @return new frame
	 */
	public Frame create(String serviceURL, String operationID) {

		Frame frame = new Frame();
		ServiceFunction action = new ServiceFunction();
		action.setServiceName(serviceURL);
		action.setFunctionName(operationID);
		return this.map(action, frame);

	}

	/**
	 * Matches a frame with a service function. Generates missing slots for the
	 * frame consistent with the service function attributes.
	 * 
	 * @param modelAction modeled service action
	 * @param frame modeled frame
	 * @return matched frame
	 */
	public Frame map(ServiceFunction modelAction, Frame frame) {

		assert modelAction != null;
		assert frame != null;
		
		System.out.println("Mapping service action " + modelAction.getFunctionName() + " into frame " + frame.getName());
		
		// Merge model function definition and swagger received definition
		ServiceFunction swaggerAction = OpenAPIConnector.readFunction(modelAction);
		ServiceFunction frameAction = swaggerAction.merge(modelAction);
		frame.setServiceFunction(frameAction);

		// Generating slots
		for (ServiceFunctionAttribute attr : frameAction.getAttributes()) {
			Slot slot = map(attr, attr.getName());
			frame.addSlot(slot);
		}
		
		// Fill missing frame definitions
		if (frame.getIntent() == null)
			frame.setIntent(frameAction.getFunctionName());

		if (frame.getMessage() == null)
			frame.setMessage(frameAction.getFunctionDescription());

		frame.invariant();
		return frame;
	}

	/**
	 * Maps a service function attribute into a new slot.
	 * 
	 * @param attr of service function
	 * @param name of new slot
	 * @return generated slot
	 */
	public Slot map(ServiceFunctionAttribute attr, String name) {

		assert attr != null : "map attr with slot: attr is null";

		// create slot
		Slot slot = new Slot(name);
		slot.setParameter(attr);
		slot.setRequired(attr.isRequired());

		// content type
		if (attr.getContentType() != null)
			switch (attr.getContentType().toLowerCase()) {
			case "enum":
				slot.setInputType(InputType.Enum);
				break;
			case "integer":
				slot.setInputType(InputType.Number);
				break;
			case "url":
				slot.setInputType(InputType.Url);
				break;
			default:
				slot.setInputType(InputType.Free);
			}
		else
			slot.setInputType(InputType.Free);

		if(attr.getEntity() != null)
			slot.setEntity(attr.getEntity());
		
		// selection
		if (attr.hasDiscriminator()) {
			slot.setSelection(false);
			ServiceFunctionAttribute disAttr = attr.getDiscriminatorAttribute();
			Slot disSlot = map(disAttr, name + "_" + disAttr.getName());
			disSlot.setSelection(true);
			disSlot.setRequired(true);
			for (String enu : disAttr.getEnumList()) {
				Slot enuSlot = new Slot(name + "_" + disAttr.getName() + "_" + enu);
				enuSlot.setEntity(enu);
				disSlot.addChild(enuSlot);
				for (ServiceFunctionAttribute subAttr : attr.getChildren(enu)) {
					Slot subSlot = map(subAttr, name + "_" + disAttr.getName() + "_" + enu + "_" + subAttr.getName());
					enuSlot.addChild(subSlot);
				}
			}

			slot.addChild(disSlot);

		} else // children
		if (attr.hasChildren())
			for (ServiceFunctionAttribute subAttr : attr.getChildAttributes()) {
				Slot subSlot = map(subAttr, name + "_" + subAttr.getName());
				slot.addChild(subSlot);
			}

		return slot;
	}

}
