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
			if (attr.getContentType() != null)
				switch (attr.getContentType()) {
				case "enum":
					slot.setInputType(ExpectedInputType.Enum);
					break;
				case "integer":
					slot.setInputType(ExpectedInputType.Number);
					break;
				default:
					slot.setInputType(ExpectedInputType.Free);
				}
			else
				slot.setInputType(ExpectedInputType.Free);

			frame.addSlot(slot);

			// children
			for (ServiceFunctionAttribute subattr : attr.getChildAttributes()) {
				Slot childSlot = new Slot(attr.getName() + "_" + subattr.getName());
				childSlot.setParameter(subattr);
				childSlot.setNlu_intent("inform_" + attr.getName() + "_" + subattr.getName());
				childSlot.setNlg_intent("request_" + attr.getName() + "_" + subattr.getName());
				childSlot.setRequired(subattr.isRequired());

				switch (subattr.getContentType()) {
				case "enum":
					childSlot.setInputType(ExpectedInputType.Enum);
					break;
				case "integer":
					childSlot.setInputType(ExpectedInputType.Number);
					break;
				case "string":
					if (childSlot.getParameter().getName().contains("Url")) {
						childSlot.setInputType(ExpectedInputType.Url);
						break;
					}
				default:
					childSlot.setInputType(ExpectedInputType.Free);
				}

				// children of children TODO recursive function
				for (ServiceFunctionAttribute subsubattr : subattr.getChildAttributes()) {
					Slot childchildSlot = new Slot(
							attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());
					childchildSlot.setParameter(subsubattr);
					childchildSlot.setNlu_intent(
							"inform_" + attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());
					childchildSlot.setNlg_intent(
							"request_" + attr.getName() + "_" + subattr.getName() + "_" + subsubattr.getName());

					switch (subsubattr.getContentType()) {
					case "enum":
						childchildSlot.setInputType(ExpectedInputType.Enum);
						break;
					case "integer":
						childchildSlot.setInputType(ExpectedInputType.Number);
						break;
					default:
						childchildSlot.setInputType(ExpectedInputType.Free);
					}

					childchildSlot.setRequired(subsubattr.isRequired());
					childSlot.addChild(childchildSlot);

				}

				slot.addChild(childSlot);
			}

		}

		return frame;
	}

}
