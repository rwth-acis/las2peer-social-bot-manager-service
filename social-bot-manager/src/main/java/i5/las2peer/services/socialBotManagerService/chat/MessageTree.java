package i5.las2peer.services.socialBotManagerService.chat;

import java.util.ArrayList;
import java.util.HashSet;


public class MessageTree {
	private String postid; 
	private String userid; 
	private String originpid; // Original post ID
	private String originuid; // Original user ID (both IDs form the sequence ID)
	private MessageTree parent;
	private boolean linked; // Tells us whether the post has a child of the same sequence
	private ArrayList<MessageTree> children;
	private static HashSet<String> ignoreIds = new HashSet<String>();
	
	public static void setIgnoreId(String id) {
		ignoreIds.add(id);
	}
	
	public static boolean hasIgnoreId(String id) {
		return ignoreIds.contains(id);
	}
	
	public String getUserId() {
		return this.userid;
	}
	
	public String getPostId() {
		return this.postid;
	}
	
	public String getOriginPid() {
		return this.originpid;
	}
	
	public String getOriginUid() {
		return this.originuid;
	}
	
	public void setLinked(boolean val) {
		this.linked = val;
	}
	
	public boolean isLinked() {
		return this.linked;
	}
	
	public MessageTree (String postid, String userid, MessageTree parent) {
		this.postid = postid;
		this.userid = userid;
		this.parent = parent;
		this.linked = false;
		this.children = new ArrayList<MessageTree>();
		this.setOrigin();
		//System.out.println("\u001B[33mDebug --- Origin: " + this.postid + " " + this.userid + "\u001B[0m");
	}
	
	private void setOrigin() {
		if (this.parent != null) {
			// If post comes from a bot or teacher
			if (ignoreIds.contains(this.userid)) {
				this.originpid = parent.getOriginPid();
				this.originuid = parent.getOriginUid();
				this.parent.setLinked(true);
				//System.out.println("\u001B[33mDebug --- User: 1\u001B[0m");
			// If post comes from a student
			} else {
				// If parent is a bot or teacher and has no child in the sequence and post is made by the same user as origin, link post to its sequence
				//System.out.println("\u001B[33mDebug --- Ignores: " + ignoreIds + "\u001B[0m");
				//System.out.println("\u001B[33mDebug --- Linked: " + this.parent.isLinked() + "\u001B[0m");
				//System.out.println("\u001B[33mDebug --- User: " + this.userid.equals(this.parent.getOriginUid()) + "\u001B[0m");
				if (ignoreIds.contains(this.parent.getUserId()) && !this.parent.isLinked() && this.userid.equals(this.parent.getOriginUid())) {
					this.originpid = parent.getOriginPid();
					this.originuid = parent.getOriginUid();
					this.parent.setLinked(true);
				// If parent is a student or already has a child in the sequence or post's user is not the origin user, post starts its own sequence
				} else {
					//System.out.println("\u001B[33mDebug --- User: 3\u001B[0m");
					this.originpid = this.postid;
					this.originuid = this.userid;
				}
			}
		// If post is a root (no parent), it starts its own sequence
		} else {
			//System.out.println("\u001B[33mDebug --- User: 4\u001B[0m");
			this.originpid = this.postid;
			this.originuid = this.userid;
		}
	}
	
	private void addChild(MessageTree child) {
		this.children.add(child);
	}
	
	public MessageTree searchPost(String postid) {
		if (this.postid.equals(postid)) {
			return this;
		} else {
			for (MessageTree tree : this.children) {
				MessageTree res = tree.searchPost(postid);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
	
	public MessageTree getSequenceTail() {
		for (MessageTree child : this.children) {
			if (child.getOriginPid().equals(this.originpid)) {
				return child.getSequenceTail();
			}
		}
		return this;
	}
	
	public boolean insertPost(String postid, String userid, String parentid) {
		MessageTree parentTree = this.searchPost(parentid);
		if (parentTree != null) {
			parentTree.addChild(new MessageTree(postid, userid, parentTree));
			System.out.println("\u001B[33mDebug --- Post inserted: " + postid + " " + userid + "\u001B[0m");
			return true;	
		}
		else {
			return false;
		}
	}
	
	public boolean containsPost(String postid) {
		return searchPost(postid) != null;
	}
}

