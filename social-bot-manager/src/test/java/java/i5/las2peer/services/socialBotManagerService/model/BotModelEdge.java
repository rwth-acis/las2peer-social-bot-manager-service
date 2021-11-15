package i5.las2peer.services.socialBotManagerService.model;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class BotModelEdge implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4613520960473047763L;
	private BotModelLabel label;
	private String source;
	private String target;
	private LinkedHashMap<String, BotModelNodeAttribute> attributes;
	private String type;
	public BotModelLabel getLabel() {
		return label;
	}
	public void setLabel(BotModelLabel label) {
		this.label = label;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public LinkedHashMap<String, BotModelNodeAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(LinkedHashMap<String, BotModelNodeAttribute> attributes) {
		this.attributes = attributes;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
