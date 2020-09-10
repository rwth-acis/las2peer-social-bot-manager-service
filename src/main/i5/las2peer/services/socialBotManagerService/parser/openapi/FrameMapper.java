package i5.las2peer.services.socialBotManagerService.parser.openapi;

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
			Slot slot = new Slot();
			slot.setParameter(attr);
			slot.setName(attr.getName());			
			slot.setNlu_intent("inform_" + attr.getName());
			slot.setNlg_intent("request_" + attr.getName());
			frame.addSlot(slot);
			
			String message = "We need to know - name: ".concat(attr.getName());
			if (attr.getDescription() != null)
				message = message.concat(" description: ").concat(attr.getDescription());
			if (attr.getContentType() != null)
				message = message.concat("contentType: ").concat(attr.getContentType());
			if (attr.getExample() != null)
				message = message.concat("example: ").concat(attr.getExample());
			if (attr.getEnumList() != null && attr.getEnumList().size() > 0) {
				message = message.concat("possible answers: ");
				for (Object enu : attr.getEnumList()) {
					message = message.concat(", ").concat((String) enu);
				}
				slot.setEntity(attr.getName());
			}
			slot.setMessage(message);

			for (ServiceFunctionAttribute subattr : attr.getChildAttributes()) {
				Slot childSlot = new Slot();
				childSlot.setParameter(subattr);
				childSlot.setName(subattr.getName());
				childSlot.setNlu_intent("inform_" + attr.getName() + "_" + subattr.getName());
				childSlot.setNlg_intent("request_" + attr.getName() + "_" + subattr.getName());

				message = "We need to know - Name: ".concat(subattr.getName());
				if (subattr.getDescription() != null)
					message = message.concat(" Description: ").concat(subattr.getDescription());
				if (subattr.getContentType() != null)
					message = message.concat("ContentType: ").concat(subattr.getContentType());
				if (subattr.getExample() != null)
					message = message.concat("Example: ").concat(subattr.getExample());
				if (subattr.getEnumList() != null && subattr.getEnumList().size() > 0) {
					message = message.concat("possible answers: ");
					for (Object enu : subattr.getEnumList()) {
						message = message.concat(", ").concat((String) enu);
					}
					childSlot.setEntity(subattr.getName());
				}
				childSlot.setMessage(message);
				slot.addChildren(childSlot);
			}

		}
		System.out.println(frame.toString());
		return frame;
	}

}
