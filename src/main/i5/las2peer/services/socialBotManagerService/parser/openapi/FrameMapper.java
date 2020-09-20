package i5.las2peer.services.socialBotManagerService.parser.openapi;

import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class FrameMapper {

    public Frame create(ServiceFunction action, Frame frame) {

	System.out.println("parsing parameter information of service action");
	action = OpenAPIConnector.readFunction(action);
	System.out.println("number of parameters: " + action.getAttributes().size());
	frame.setServiceFunction(action);
	frame = map(action, frame);
	return frame;
    }

    public Frame map(ServiceFunction action, Frame frame) {

	System.out.println("mapping service action into frame");
	for (ServiceFunctionAttribute attr : action.getAttributes()) {

	    System.out.println("add slot");
	    Slot slot = new Slot(attr.getName());
	    slot.setParameter(attr);
	    slot.setNlu_intent("inform_" + attr.getName());
	    slot.setNlg_intent("request_" + attr.getName());
	    slot.setRequired(attr.isRequired());
	    switch(attr.getContentType()) {
	    case "enum":
		slot.setInputType(ExpectedInputType.Enum);
		break;
	    case "integer":
		slot.setInputType(ExpectedInputType.Number);
		break;
	    default: 
		slot.setInputType(ExpectedInputType.Free);
	    }
	    frame.addSlot(slot);

	    String message = "I want to know \n name: ".concat(attr.getName()).concat("\n");
	    if (attr.getDescription() != null)
		message = message.concat("description: ").concat(attr.getDescription()).concat("\n");
	    if (attr.getParameterType() != null)
		message = message.concat("parameterType: ").concat(attr.getContentType()).concat("\n");
	    if (attr.getExample() != null)
		message = message.concat("example: ").concat(attr.getExample()).concat("\n");
	    if (attr.getEnumList() != null && attr.getEnumList().size() > 0) {
		message = message.concat("possible answers: ");
		for (Object enu : attr.getEnumList()) {
		    message = message.concat(", ").concat((String) enu);
		}
		slot.setEntity(attr.getName());
	    }
	    slot.setMessage(message);

	  
	    
	    // children
	    for (ServiceFunctionAttribute subattr : attr.getChildAttributes()) {
		Slot childSlot = new Slot(attr.getName() + "_" + subattr.getName());
		childSlot.setParameter(subattr);
		childSlot.setNlu_intent("inform_" + attr.getName() + "_" + subattr.getName());
		childSlot.setNlg_intent("request_" + attr.getName() + "_" + subattr.getName());
		childSlot.setRequired(subattr.isRequired());

		 switch(subattr.getContentType()) {
		    case "enum":
			childSlot.setInputType(ExpectedInputType.Enum);
			break;
		    case "integer":
			childSlot.setInputType(ExpectedInputType.Number);
			break;
		    default: 
			childSlot.setInputType(ExpectedInputType.Free);
		    }
		
		message = "I want to know \n name: ".concat(subattr.getName()).concat("\n");
		if (subattr.getDescription() != null)
		    message = message.concat(" Description: ").concat(subattr.getDescription()).concat("\n");
		if (subattr.getContentType() != null)
		    message = message.concat("ContentType: ").concat(subattr.getContentType()).concat("\n");
		if (subattr.getExample() != null)
		    message = message.concat("Example: ").concat(subattr.getExample()).concat("\n");
		if (subattr.getEnumList() != null && subattr.getEnumList().size() > 0) {
		    message = message.concat("possible answers: ");
		    for (Object enu : subattr.getEnumList()) {
			message = message.concat(", ").concat((String) enu);
		    }
		    childSlot.setEntity(subattr.getName());
		}
		childSlot.setMessage(message);

		// children of children TODO recursive function
		for (ServiceFunctionAttribute subsubattr : subattr.getChildAttributes()) {
		    Slot childchildSlot = new Slot(attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());
		    childchildSlot.setParameter(subsubattr);
		    childchildSlot.setNlu_intent(
			    "inform_" + attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());
		    childchildSlot.setNlg_intent(
			    "request_" + attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());
		    
			 switch(subsubattr.getContentType()) {
			    case "enum":
				childchildSlot.setInputType(ExpectedInputType.Enum);
				break;
			    case "integer":
				childchildSlot.setInputType(ExpectedInputType.Number);
				break;
			    default: 
				childchildSlot.setInputType(ExpectedInputType.Free);
			    }
		    
		    message = "I want to know \n name: ".concat(subsubattr.getName()).concat("\n");
		    if (subsubattr.getDescription() != null)
			message = message.concat(" Description: ").concat(subsubattr.getDescription()).concat("\n");
		    if (subsubattr.getContentType() != null)
			message = message.concat("ContentType: ").concat(subsubattr.getContentType()).concat("\n");
		    if (subsubattr.getExample() != null)
			message = message.concat("Example: ").concat(subsubattr.getExample()).concat("\n");
		    if (subsubattr.getEnumList() != null && subsubattr.getEnumList().size() > 0) {
			message = message.concat("possible answers: ");
			for (Object enu : subsubattr.getEnumList()) {
			    message = message.concat(", ").concat((String) enu);
			}
			childchildSlot.setEntity(subsubattr.getName());
		    }
		    childchildSlot.setMessage(message);
		    childchildSlot.setRequired(subsubattr.isRequired());
		    childSlot.addChild(childchildSlot);

		}

		slot.addChild(childSlot);
	    }

	}
	System.out.println(frame.toString());
	return frame;
    }

}
