package i5.las2peer.services.socialBotManagerService.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

import org.json.JSONArray;

import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RocketChatMessage.Type;

public class RocketChatMessageCollector extends ChatMessageCollector {

	public void handle(RocketChatMessage message) {
		Type type = message.getMsgType();
		if (type != null) {
			if (type.equals(Type.TEXT)) {
				try {
					System.out.println("Handling text.");
					JSONArray emails = message.getSender().getEmails();
					// System.out.println(emails.toString());
					String rid = message.getRoomId();
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(message.getMessage());
					ChatMessage cm = new ChatMessage(rid, user, msg);
					// timestamp
					cm.setTime(message.getMsgTimestamp().toInstant().toString());
					// domain
					cm.setDomain(this.getDomain());

					this.addMessage(cm);
					System.out.println("Message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Unsupported type: " + type.toString());
			}
		} else {
			System.out.println("Skipped");
		}
	}

	public void handle(RocketChatMessage message, int role, String email) {
		Type type = message.getMsgType();
		if (type != null) {
			if (type.equals(Type.TEXT)) {
				try {
					System.out.println("Handling text.");
					JSONArray emails = message.getSender().getEmails();
					// System.out.println(emails.toString());
					String rid = message.getRoomId();
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(message.getMessage());
					ChatMessage cm = new ChatMessage(rid, user, msg);
					System.out.println("Email of user is " + email);
					cm.setEmail(email);
					cm.setRole(role);
					// timestamp
					cm.setTime(message.getMsgTimestamp().toInstant().toString());
					// domain
					cm.setDomain(this.getDomain());

					this.addMessage(cm);
					System.out.println("Message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(type.equals(Type.MESSAGE_EDITED)){
				try {
					System.out.println("Handling edited message.");
					JSONArray emails = message.getSender().getEmails();
					// System.out.println(emails.toString());
					String rid = message.getRoomId();
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(message.getMessage());
					ChatMessage cm = new ChatMessage(rid, user, msg);
					// timestamp
					cm.setTime(message.getMsgTimestamp().toInstant().toString());
					// domain
					cm.setDomain(this.getDomain());

					this.addMessage(cm);
					System.out.println("Edited message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Unsupported type: " + type.toString());
			}
		} else {
			System.out.println("Skipped");
		}
	}

	public void handle(RocketChatMessage message, String fileBody, String fileName, String fileType, int role,
			String email) {
		Type type = message.getMsgType();
		if (type != null) {
			if (type.equals(Type.ATTACHMENT)) {
				try {
					System.out.println("Handling Attachment.");
					JSONArray emails = message.getSender().getEmails();
					System.out.println("rcket message is " + message);
					String rid = message.getRoomId();
					System.out.println(rid);
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(fileName);
					ChatMessage cm = new ChatMessage(rid, user, msg, fileName, fileType, fileBody);
					System.out.println("Email of user is " + email);
					cm.setEmail(email);
					cm.setRole(role);

					// timestamp
					cm.setTime(message.getMsgTimestamp().toInstant().toString());

					// domain
					cm.setDomain(this.getDomain());

					this.addMessage(cm);
					System.out.println("Message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Unsupported type: " + type.toString());
			}
		} else {
			System.out.println("Skipped");
		}
	}
}
