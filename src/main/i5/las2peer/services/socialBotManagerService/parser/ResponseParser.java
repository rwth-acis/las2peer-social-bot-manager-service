package i5.las2peer.services.socialBotManagerService.parser;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseParser {

	public String toMarkdown(String jsonString) {

		JsonElement element = JsonParser.parseString(jsonString);
		return toMarkdown(element);

	}

	public String toMarkdown(JsonElement element) {
		assert element != null;
		String res = "";

		if (element.isJsonPrimitive())
			return element.getAsString();

		if (element.isJsonObject())
			return toMarkdown((JsonObject) element);

		if (element.isJsonArray())
			return toMarkdown((JsonArray) element);

		return res;
	}

	public String toMarkdown(JsonArray array) {
		assert array != null;
		String res = "";

		for (Iterator<JsonElement> it = array.iterator(); it.hasNext();) {
			JsonElement element = it.next();
			res = res + "- " + toMarkdownSingleLine(element) + "\n";
		}
		return res;
	}

	public String toMarkdownSingleLine(JsonElement object) {
		assert object != null;
		String res = "";

		if (object.isJsonPrimitive())
			return object.getAsString();
		
		if (object.isJsonObject()) {
			for (Entry<String, JsonElement> entry : ((JsonObject)object).entrySet()) {
				JsonElement element = entry.getValue();
				res = toMarkdown(element) + " ";
			}
			res = res + "\n";
			return res;
		}
		
		return res;

	}

	public String toMarkdown(JsonObject object) {
		assert object != null;
		String res = "";
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			JsonElement element = entry.getValue();
			res = entry.getKey() + toMarkdown(element) + "\n";
		}

		return res;
	}

}
