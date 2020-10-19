package i5.las2peer.services.socialBotManagerService.parser.openapi;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class FrameMapper {

    public Frame create(ServiceFunction action, Frame frame) {

	System.out.println("parsing parameter information of service action");
	action = OpenAPIConnector.readFunction(action);
	frame.setServiceFunction(action);
	frame = map(action, frame);

	frame.invariant();
	return frame;
    }

    public Frame create(String serviceURL, String operationID) {

	Frame frame = new Frame();
	ServiceFunction action = new ServiceFunction();
	action.setServiceName(serviceURL);
	action.setFunctionName(operationID);
	return this.create(action, frame);

    }

    public Frame map(ServiceFunction action, Frame frame) {

	System.out.println("mapping service action into frame");
	for (ServiceFunctionAttribute attr : action.getAttributes()) {
	    Slot slot = map(attr, attr.getName());
	    frame.addSlot(slot);
	}

	if (frame.getIntent() == null)
	    frame.setIntent(action.getFunctionName());

	if (frame.getMessage() == null)
	    frame.setMessage(action.getFunctionDescription());

	return frame;
    }

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
	    case "string":
		if (name.contains("Url") || name.contains("URL"))
		slot.setInputType(InputType.Url);
	    default:
		slot.setInputType(InputType.Free);
	    }
	else
	    slot.setInputType(InputType.Free);

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
