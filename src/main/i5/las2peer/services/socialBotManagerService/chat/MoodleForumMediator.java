package i5.las2peer.services.socialBotManagerService.chat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.OptionalLong;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

public class MoodleForumMediator extends ChatMediator {
	private final static String domainName = "https://moodle.tech4comp.dbis.rwth-aachen.de";
	private final static HashSet<String> ignoreIds = new HashSet<String>(Arrays.asList("askabot@fakemail.de", "neumann@dbis.rwth-aachen.de"));
	private MoodleForumMessageCollector messageCollector = new MoodleForumMessageCollector();
	private HashMap<String, MessageTree> discussions = new HashMap<String, MessageTree>();
	
	public MoodleForumMediator(String authToken) {
		super(authToken);
		MessageTree.setIgnoreIds(ignoreIds);
	}
	
	@Override
	public void sendMessageToChannel(String channel, String text, Optional<String> id) {
		try {
			// Get sequence IDs and find origin post
			HashMap<String,String> args = new HashMap<String,String>();
			args.put("message", text);
			args.put("subject", "Bot response");
			
			String originpid = id.get();
			//System.out.println("\u001B[33mDebug --- Origin PID: " + originpid + "\u001B[0m");
			if (discussions.containsKey(channel)) {
				MessageTree originPost = discussions.get(channel).searchPost(originpid);
				if (originPost != null) {
					String postid = originPost.getSequenceTail().getPostId();
					args.put("postid", postid); 
					String res = sendRequest(domainName, "mod_forum_add_discussion_post", args);
					System.out.println("\u001B[33mDebug --- Post found in tree: " + postid + "\u001B[0m");
				} else {
					args.put("postid", originpid);
					String res = sendRequest(domainName, "mod_forum_add_discussion_post", args);
					System.out.println("Debug --- Post not in tree: " + originpid);
				}
			} else {
				args.put("postid", originpid);
				String res = sendRequest(domainName, "mod_forum_add_discussion_post", args);
				System.out.println("Debug --- No discussion tree: " + originpid);
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
			if (verbID.contains("posted") || verbID.contains("replied")) {
				JSONObject obj = (JSONObject) json.get("object");
				JSONObject definition = (JSONObject) obj.get("definition");
				JSONObject description = (JSONObject) definition.get("description");
				
				
				String type = definition.getString("type");
				String message = description.getString("en-US");
				
				JSONObject actor = (JSONObject) json.get("actor");
				JSONObject account = (JSONObject) actor.get("account");
				
				String userEmail = account.getString("name");
				try {
					if (type.contains("discussion")) {
						String discussionid = obj.getString("id").split("d=")[1];
						
						// Query all posts in the discussion and choose the earliest one (origin post)
						HashMap<String,String> args = new HashMap<String,String>();
						args.put("discussionid", discussionid);
						args.put("sortdirection", "ASC");
						String res = sendRequest(domainName, "mod_forum_get_discussion_posts", args);
						//System.out.println("Debug --- Posts: " + res);
						JSONObject resObj = new JSONObject(res);
						JSONArray posts = (JSONArray) resObj.get("posts");
						JSONObject post = (JSONObject) posts.get(0);
						
						// The ID of a sequence is the post ID
						String postid = Integer.toString(post.getInt("id"));
						
						// Add new post tree (discussion)
						MessageTree newPost = new MessageTree(postid, userEmail, null);
						discussions.put(discussionid, newPost);
						
						this.messageCollector.handle(discussionid, postid, message);
					} else {
						String discussionid = obj.getString("id").split("d=")[1].split("#")[0];
						String postid = obj.getString("id").split("#p")[1];
						
						if (discussions.containsKey(discussionid)) {
							HashMap<String,String> args = new HashMap<String,String>();
							args.put("postid", postid);
							String res = sendRequest(domainName, "mod_forum_get_discussion_post", args);
							JSONObject resObj = new JSONObject(res);
							JSONObject post = (JSONObject) resObj.get("post");
							String parentid = Integer.toString(post.getInt("parentid"));
							
							// Add post to existing tree
							//System.out.println("\u001B[33mDebug --- Parent ID: " + parentid + "\u001B[0m");
							if (discussions.get(discussionid).insertPost(postid, userEmail, parentid)) {
								// Add message to collector with post ID of the original post
								//discussions.get(discussionid).insertPost(postid, userEmail, parentid);
								
								
								String originid = discussions.get(discussionid).searchPost(postid).getOriginPid();
								if (!ignoreIds.contains(userEmail)) {
									this.messageCollector.handle(discussionid, originid, message);
								}
							
							// If no parent could be found (for example, if parent message was not received by the service)
							} else {
								if (!ignoreIds.contains(userEmail)) {
									this.messageCollector.handle(discussionid, postid, message);
								}
								System.out.println("Error: Origin post not found (postid = " + postid + ")");
							}
							
						// If discussion does not exist (for example, because the service stopped), 
						} else {
							if (!ignoreIds.contains(userEmail)) {
								this.messageCollector.handle(discussionid, postid, message);
							}
							System.out.println("Error: Discussion tree not initialized (postid = " + postid + ")");
						}
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
	
	private void buildMessageTree() {
		
	}
}
