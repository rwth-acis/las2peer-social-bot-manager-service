package i5.las2peer.services.socialBotManagerService.chat;

import org.json.JSONObject;

import java.io.File;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

public class MoodleForumMediator extends ChatMediator {
	private final static String domainName = "https://moodle.tech4comp.dbis.rwth-aachen.de";
	private final static HashSet<String> ignoreIds = new HashSet<String>(Arrays.asList("148", "75"));//TODO: remove now that these are automatically defined
	private MoodleForumMessageCollector messageCollector = new MoodleForumMessageCollector();
	private HashMap<String, MessageTree> discussions = new HashMap<String, MessageTree>();
	//private String signature = "<pre><i>This message was sent by an assistant chatbot. Please consider to fill in a survey about its performance under <a href=' https://limesurvey.tech4comp.dbis.rwth-aachen.de/index.php/253638?lang=en'>this link</a>.</i></pre>";
	
	public MoodleForumMediator(String authToken) {
		super(authToken);
		MessageTree.setIgnoreIds(ignoreIds);
	}
	
	@Override
	public void sendMessageToChannel(String channel, String text, Optional<String> id) {
		try {
			// Get sequence IDs and find origin post
			HashMap<String,String> args = new HashMap<String,String>();
			boolean shouldPost = false;
			args.put("message", text);
			args.put("subject", "Bot response");
			
			String originpid = id.get();
			if (discussions.containsKey(channel)) {
				MessageTree originPost = discussions.get(channel).searchPost(originpid);
				if (originPost != null) {
					String postid = originPost.getSequenceTail().getPostId();
					args.put("postid", postid); 
					shouldPost = true;
					System.out.println("\u001B[33mDebug --- Post found in tree: " + postid + "\u001B[0m");
				} else {
					args.put("postid", originpid);
					shouldPost = true;
					System.out.println("Debug --- Post not in tree: " + originpid);
				}
			} else {
				args.put("postid", originpid);
				shouldPost = true;
				System.out.println("Debug --- No discussion tree: " + originpid);
			}
			if (shouldPost) {
				String res = sendRequest(domainName, "mod_forum_add_discussion_post", args);
				JSONObject respObj = new JSONObject(res);
				JSONObject postObj = (JSONObject) respObj.get("post");
				String botid = postObj.getString("id");
				if (!ignoreIds.contains(botid)) {
					ignoreIds.add(botid);
				}
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(ArrayList<String> statements) {
		for (String statement : statements) {
			String xAPIStatement = statement.split("\\*")[0];
			JSONObject json = new JSONObject(xAPIStatement);
			JSONObject verb = (JSONObject) json.get("verb");
			String verbID = verb.getString("id");
			
			// If the statement is about forum activity
			if (verbID.contains("replied") || verbID.contains("posted")) {
				JSONObject obj = (JSONObject) json.get("object");
				JSONObject definition = (JSONObject) obj.get("definition");
				JSONObject description = (JSONObject) definition.get("description");
				JSONObject actor = (JSONObject) json.get("actor");
				JSONObject account = (JSONObject) actor.get("account");
				
				JSONObject context = (JSONObject) json.get("context");
				JSONObject extensions = (JSONObject) context.get("extensions");
				
				String message = description.getString("en-US");
				String userid = account.getString("name");
				try {
					String discussionid = obj.getString("id").split("d=")[1].split("#")[0];
					String postid;
					String parentid;
					if (verbID.contains("replied")) {
						postid = obj.getString("id").split("#p")[1];
						parentid = Integer.toString(((JSONObject) extensions.get("\"https://tech4comp.de/xapi/context/extensions/postParentID")).getInt("parentid"));
					} else {
						postid = Integer.toString(((JSONObject) extensions.get("https://tech4comp.de/xapi/context/extensions/rootPostID")).getInt("rootpostid"));
						parentid = null;
					}
					
					if (parentid == null && !discussions.containsKey(discussionid)) {
						MessageTree newPost = new MessageTree(postid, userid, null);
						discussions.put(discussionid, newPost);
						if (!ignoreIds.contains(userid)) {
							this.messageCollector.handle(discussionid, postid, message);
						}
					} else if (discussions.containsKey(discussionid) && !discussions.get(discussionid).containsPost(postid)) {
						
						// Add post to existing tree
						//System.out.println("\u001B[33mDebug --- Parent ID: " + parentid + "\u001B[0m");
						if (discussions.get(discussionid).insertPost(postid, userid, parentid)) {
							// Add message to collector with post ID of the original post
							String originid = discussions.get(discussionid).searchPost(postid).getOriginPid();
							if (!ignoreIds.contains(userid)) {
								this.messageCollector.handle(discussionid, originid, message);
							}
						
						// If no parent could be found (for example, if parent message was not received by the service)
						} else {
							if (!ignoreIds.contains(userid)) {
								this.messageCollector.handle(discussionid, postid, message);
							}
							System.out.println("Error: Origin post not found (postid = " + postid + ")");
						}
						
					// If discussion does not exist (for example, because the service stopped), 
					} else {
						if (!ignoreIds.contains(userid) && !discussions.containsKey(discussionid)) {
							//MessageTree newPost = new MessageTree(postid, userid, null);
							//discussions.put(discussionid, newPost);
							this.messageCollector.handle(discussionid, postid, message);
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
	public Vector<ChatMessage> getMessages() {
		return this.messageCollector.getMessages();
	}

	@Override
	public String getChannelByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
}
