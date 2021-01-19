package i5.las2peer.services.socialBotManagerService.dialogue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public enum InputType {

	Free, Word, Number, Decimal, Confirmation, Date, Date_Time, Url, Binary, Email, File, Enum, Entity;

	public static InputType fromString(String input) {

		if(input == null)
			return InputType.Free;
		
		switch (input.toLowerCase()) {
		case "enum":
			return InputType.Enum;
		case "integer":
		case "number":
			return InputType.Number;
		case "url":
			return InputType.Url;
		case "file":
			return InputType.File;
		default:
			return InputType.Free;
		}

	}

	public boolean validate(String input) {

		switch (this) {
		case Number:
			try {
				Integer x = Integer.valueOf(input);
			} catch (Exception e) {
				return false;
			}
			break;
		case Decimal:
			try {
				Double x = Double.parseDouble(input);
			} catch (Exception e) {
				return false;
			}
			break;
		case Word:
			if (input.contains(" "))
				return false;
			break;
		case Url:
			try {
				new URL(input).toURI();
			} catch (MalformedURLException | URISyntaxException e) {
				return false;
			}
			break;
		}
		return true;
	}

}
