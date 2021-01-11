package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.TableLanguageGenerator;
import i5.las2peer.services.socialBotManagerService.parser.creation.CreationFunction;

public class Domain {

	/**
	 * name of this domain
	 */
	String name;

	/**
	 * description of this domain
	 */
	String description;

	/**
	 * frames of this domain
	 */
	Map<String, Frame> frames;

	/**
	 * incoming messages of this domain
	 */
	Map<String, IncomingMessage> messages;

	/**
	 * selections of this domain
	 */
	Map<String, Selection> selections;

	public Domain(String name) {
		super();
		this.name = name;
		this.frames = new HashMap<String, Frame>();
		this.messages = new HashMap<String, IncomingMessage>();
		this.selections = new HashMap<String, Selection>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<MessengerElement> getMessengerElements() {
		Collection<MessengerElement> res = new HashSet<>();
		for (MessengerElement element : this.frames.values())
			res.add(element);
		for (MessengerElement element : this.messages.values())
			res.add(element);
		for (MessengerElement element : this.selections.values())
			res.add(element);
		return res;
	}

	public void add(MessengerElement element) {
		if (element instanceof Frame)
			add((Frame) element);
		if (element instanceof Selection)
			add((Selection) element);
		if (element instanceof IncomingMessage)
			add((IncomingMessage) element);
	}

	public void add(Frame frame) {
		this.frames.put(frame.getIntentKeyword(), frame);
	}

	public void add(IncomingMessage message) {
		this.messages.put(message.getEntityKeyword(), message);
	}

	public void add(Selection selection) {
		selections.put(selection.getIntentKeyword(), selection);
	}

	public void add(Collection<MessengerElement> elements) {
		for (MessengerElement element : elements)
			this.add(element);
	}

	public LanguageGenerator generateLanguageGenerator() {
		TableLanguageGenerator generator = new TableLanguageGenerator();
		for (IncomingMessage message : this.messages.values()) {
			generator.addEntry(message.getIntentKeyword(), message.getResponseMessage());
		}
		return generator;
	}

	public Map<String, IncomingMessage> getMessages() {
		return messages;
	}

	public Map<String, Frame> getFrames() {
		return frames;
	}

	public Map<String, Selection> getSelections() {
		return selections;
	}

	public Collection<Command> getCommands() {

		Collection<Command> res = new ArrayList<>();
		for (MessengerElement element : getMessengerElements())
			if (element.isOperation())
				res.add(((Menuable) element).getCommand());

		return res;
	}

	public Collection<String> getNLUIntents() {
		Collection<String> res = new HashSet<>();
		for (MessengerElement element : getMessengerElements())
			res.add(element.getIntentKeyword());
		res.remove("");
		return res;
	}

	public Map<String, CreationFunction> getCreationFunctions() {

		Map<String, CreationFunction> res = new HashMap<>();
		if (this.frames != null)
			for (Frame frame : this.frames.values()) {
				CreationFunction function = new CreationFunction(frame.getName(), "Service Access");
				res.put(function.getName(), function);
			}

		if (this.messages != null && !this.messages.isEmpty()) {
			CreationFunction function = new CreationFunction(this.getName(), "Chit Chat");
			res.put(function.getName(), function);
		}

		return res;
	}

	public boolean deleteCreationFunction(String name) {
		assert name != null;

		if (this.frames != null)
			for (Entry<String, Frame> entry : this.frames.entrySet()) {
				String key = entry.getKey();
				Frame frame = entry.getValue();
				if (name.contentEquals(frame.getName())) {
					this.frames.remove(key);
					return true;
				}
			}

		if (this.messages != null && name.contentEquals(this.getName())) {
			this.messages.clear();
			return true;
		}
		
		return false;

	}

	public boolean validate() {
		if (this.name == null)
			return false;
		if (this.frames == null)
			return false;
		if (this.messages == null)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Domain [name=" + name + ", frames=" + frames + ", messages=" + messages + "]";
	}

}
