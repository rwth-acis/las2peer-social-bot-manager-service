package services.socialBotManagerService.model;

import java.io.Serializable;
import java.util.LinkedHashMap;


public class BotModelAttribute implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3505476346232276390L;
	private BotModelLabel label;
	private double left;
	private double top;
	private double width;
	private double height;
	private double zIndex;
	private String type;
	private LinkedHashMap<String,BotModelNodeAttribute> attributes;
	public BotModelLabel getLabel() {
		return label;
	}
	public void setLabel(BotModelLabel label) {
		this.label = label;
	}
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
	}
	public double getTop() {
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public double getzIndex() {
		return zIndex;
	}
	public void setzIndex(double zIndex) {
		this.zIndex = zIndex;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public LinkedHashMap<String,BotModelNodeAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(LinkedHashMap<String,BotModelNodeAttribute> attributes) {
		this.attributes = attributes;
	}
}