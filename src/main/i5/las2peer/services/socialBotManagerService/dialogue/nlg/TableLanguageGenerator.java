package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.ws.rs.core.MultivaluedHashMap;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;

public class TableLanguageGenerator extends LanguageGenerator {

	private Map<String, List<String>> table;

	public TableLanguageGenerator() {
		this.table = new MultivaluedHashMap<String, String>();
	}

	@Override
	public ResponseMessage parse(DialogueAct act) {

		assert this.table != null : "parse dialogue act. table is null";
		assert act != null : "parse dialogue act. act is null";
		assert act.getIntent() != null : "parse dialogue act. act has no intent";
		assert invariant() : "parse dialogue act. invalid state";

		String intent = act.getIntent();

		// unknown intent
		if (!table.containsKey(intent)) {
			System.out.println("nlg intent: " + intent + " unknown for table language generator.");
			return null;
		}

		// get entry from table
		String res = "";
		List<String> list = table.get(intent);
		if (list.size() == 1)
			res = list.get(0);
		else {
			Random rand = new Random();
			res = list.get(rand.nextInt(list.size()));
		}

		// parse entities
		if (act.hasEntities()) {
			System.out.println("parse entities");
			for (Entry<String, String> entity : act.getEntities().entrySet()) {
				System.out.println("entity:" + entity.getKey() + " " + entity.getValue());
				res = res.replaceAll("#" + entity.getKey(), entity.getValue());				
			}
		}
		
		if (act.getExpected() != null && act.getExpected().getEnums() != null) {
			String enums = "";
			for(String enu : act.getExpected().getEnums()) {
				enums = enums + enu + ", ";
			}
			if(enums.endsWith(", "))
				enums = enums.substring(0, enums.length()-2);
			res = res.replaceAll("#enums", enums);		
		}
		
		System.out.println("response:" + res);
		ResponseMessage message = new ResponseMessage(res);
		return message;
	}

	/**
	 * @param intent
	 * @param message
	 */
	@Override
	public void addEntry(String intent, String message) {

		assert this.table != null : "add nlg entry. table is null";
		assert intent != null : "add nlg entry. intent is null";
		assert message != null : "add nlg entry. message is null";
		assert !intent.equals("") : "add nlg entry. empty intent";
		assert !message.equals("") : "add nlg entry. empty message";
		
		if (this.table.containsKey(intent)) {
			List<String> values = table.get(intent);
			values.add(message);
			table.put(intent, values);
		} else {
			List<String> values = new ArrayList<String>();
			values.add(message);
			table.put(intent, values);
		}

		assert invariant() : "add nlg entry. invalid state";
	}

	/**
	 * @param intent
	 * @return
	 */
	public List<String> getEntry(String intent) {
		assert this.table != null : "get entry. table is null";
		return this.table.get(intent);
	}

	/**
	 * @return true if this object is in a valid state
	 */
	public boolean invariant() {

		if (this.table == null)
			return false;

		for (List<String> list : table.values()) {
			if (list == null || list.isEmpty())
				return false;
		}

		return true;
	}

}
