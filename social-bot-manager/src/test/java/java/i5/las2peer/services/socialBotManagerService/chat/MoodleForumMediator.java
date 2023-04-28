package i5.las2peer.services.socialBotManagerService.chat;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.json.JSONObject;

import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;

import org.json.JSONArray;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

public class MoodleForumMediator extends ChatMediator {
	private final static String domainName = "https://moodle.tech4comp.dbis.rwth-aachen.de";
	private MoodleForumMessageCollector messageCollector = new MoodleForumMessageCollector();
	private HashMap<String, MessageTree> discussions = new HashMap<String, MessageTree>();
	
	public MoodleForumMediator(String authToken) throws IOException, AuthTokenException{
		super(authToken);
		// test whether given authToken is valid
		HashMap<String,String> args = new HashMap<String,String>();
		try {
			String response = sendRequest(domainName, "core_webservice_get_site_info", args);
			if(response.contains("Invalid token")){
				throw new AuthTokenException("Authentication Token is faulty!");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendMessageToChannel(String channel, String text, HashMap<String, IncomingMessage> hashMap, String type, Optional<String> id) {
		try {
			// Get sequence IDs and find origin post
			HashMap<String,String> args = new HashMap<String,String>();
			boolean shouldPost = false;
			boolean noDiscussion = true;
			args.put("message", text);
			args.put("subject", "Bot response");
			
			for (Entry<String, MessageTree> entry : discussions.entrySet()) {
				MessageTree discussion = entry.getValue();
				if (discussion.containsPost(channel)) {
					noDiscussion = false;
					MessageTree originPost = discussion.searchPost(channel);
					if (originPost != null) {
						String postid = originPost.getSequenceTail().getPostId();
						args.put("postid", postid); 
						shouldPost = true;
						System.out.println("\u001B[33mDebug --- Post found in tree: " + postid + "\u001B[0m");
					} else {
						args.put("postid", channel);
						shouldPost = true;
						System.out.println("Debug --- Post not in tree: " + channel);
					}
					break;
				}
			}
			if (noDiscussion) {
				args.put("postid", channel);
				shouldPost = true;
				System.out.println("Debug --- No discussion tree: " + channel);
			}
			if (shouldPost) {
				String res = sendRequest(domainName, "mod_forum_add_discussion_post", args);
				JSONObject respObj = new JSONObject(res);
				JSONObject postObj = (JSONObject) respObj.get("post");
				JSONObject authorObj = (JSONObject) postObj.get("author");
				String botid = Integer.toString(authorObj.getInt("id"));
				MessageTree.setIgnoreId(botid);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(ArrayList<String> statements) {
		for (String statement : statements) {

			JSONObject json = new JSONObject(statement);
			JSONObject statementObj = (JSONObject) json.get("statement");
			JSONObject verb = (JSONObject) statementObj.get("verb");
			String verbID = verb.getString("id");
			
			// If the statement is about forum activity
			if (verbID.contains("replied") || verbID.contains("posted")) {
				JSONObject obj = (JSONObject) statementObj.get("object");
				JSONObject definition = (JSONObject) obj.get("definition");
				JSONObject description = (JSONObject) definition.get("description");
				JSONObject actor = (JSONObject) statementObj.get("actor");
				JSONObject account = (JSONObject) actor.get("account");
				
				JSONObject context = (JSONObject) statementObj.get("context");
				JSONObject extensions = (JSONObject) context.get("extensions");
				
				String message = description.getString("en-US");
				String userEmail = account.getString("name");
				HashMap<String,String> args = new HashMap<String,String>();
				args.put("field", "email");
				args.put("values[0]", userEmail);
				String userid = "";
				try {
					String res = sendRequest(domainName, "core_user_get_users_by_field", args);
					JSONObject resObj = (JSONObject) (new JSONArray(res)).get(0);
					userid = Integer.toString(resObj.getNumber("id").intValue());
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					// Determine discussion id, post id, and parent post id
					String discussionid = obj.getString("id").split("d=")[1].split("#")[0];
					String postid;
					String parentid;
					if (verbID.contains("replied")) {
						postid = obj.getString("id").split("#p")[1];
						parentid = Integer.toString(((JSONObject) extensions.get("https://tech4comp.de/xapi/context/extensions/postParentID")).getInt("parentid"));
					} else {
						postid = Integer.toString(((JSONObject) extensions.get("https://tech4comp.de/xapi/context/extensions/rootPostID")).getInt("rootpostid"));
						parentid = null;
					}
					
					if (parentid == null && !discussions.containsKey(discussionid)) {
						MessageTree newPost = new MessageTree(postid, userid, null);
						discussions.put(discussionid, newPost);
						if (!MessageTree.hasIgnoreId(userid)) {
							this.messageCollector.handle(postid, userid, message);
						}
					} else if (discussions.containsKey(discussionid) && !discussions.get(discussionid).containsPost(postid)) {
						
						// Add post to existing tree
						//System.out.println("\u001B[33mDebug --- Parent ID: " + parentid + "\u001B[0m");
						if (discussions.get(discussionid).insertPost(postid, userid, parentid)) {
							// Add message to collector with post ID of the original post
							String originid = discussions.get(discussionid).searchPost(postid).getOriginPid();
							if (!MessageTree.hasIgnoreId(userid)) {
								this.messageCollector.handle(originid, userid, message);
							}
						
						// If no parent could be found (for example, if parent message was not received by the service)
						} else {
							if (!MessageTree.hasIgnoreId(userid)) {
								this.messageCollector.handle(postid, userid, message);
							}
							System.out.println("Error: Origin post not found (postid = " + postid + ")");
						}
						
					// If discussion does not exist (for example, because the service stopped), 
					} else {
						if (!MessageTree.hasIgnoreId(userid) && !discussions.containsKey(discussionid)) {
							//MessageTree newPost = new MessageTree(postid, userid, null);
							//discussions.put(discussionid, newPost);
							this.messageCollector.handle(postid, userid, message);
						}
						System.out.println("Error: Discussion tree not initialized (postid = " + postid + ")");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String fileType, String text,
			Optional<String> id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector<ChatMessage> getMessages() {
		return this.messageCollector.getMessages();
	}

	@Override
	public String getChannelByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, String authToken, HashMap<String, IncomingMessage> hashMap, Optional<String> id) {

	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, String authToken) {
		super.sendBlocksMessageToChannel(channel, blocks, authToken);
	}

	@Override
	public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts, Optional<String> id){

	}

	@Override
	public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts) {
		super.updateBlocksMessageToChannel(channel, blocks, authToken, ts);
	}

	@Override
	public void editMessage(String channel, String messageId, String message, Optional<String> id){}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
}
