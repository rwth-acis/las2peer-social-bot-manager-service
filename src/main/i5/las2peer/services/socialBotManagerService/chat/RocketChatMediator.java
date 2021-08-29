package i5.las2peer.services.socialBotManagerService.chat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.java_websocket.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rocketchat.common.data.lightdb.document.UserDocument;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.data.model.UserObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.RocketChatAPI.ChatRoom;
import com.rocketchat.core.callback.FileListener;
import com.rocketchat.core.callback.GetSubscriptionListener;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.callback.MessageListener.SubscriptionListener;
import com.rocketchat.core.callback.RoomListener;
import com.rocketchat.core.callback.RoomListener.GetMembersListener;
import com.rocketchat.core.callback.RoomListener.GetRoomListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RocketChatMessage.Type;
import com.rocketchat.core.model.RoomObject;
import com.rocketchat.core.model.SubscriptionObject;
import com.rocketchat.core.model.TokenObject;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;

public class RocketChatMediator extends ChatMediator implements ConnectListener, LoginListener,
		RoomListener.GetRoomListener, SubscribeListener, GetSubscriptionListener, SubscriptionListener {

	private final static String url = "https://chat.tech4comp.dbis.rwth-aachen.de";
	RocketChatAPI client;
	private String username;
	private String password;
	private String token;
	private RocketChatMessageCollector messageCollector = new RocketChatMessageCollector();
	private HashSet<String> activeSubscriptions = null;
	private RasaNlu rasa;
	private SQLDatabase database;
	private Thread checkRooms = null;
	private boolean shouldCheckRooms = false;
	private HashMap<String, Boolean> sendingMessage = new HashMap<String, Boolean>();

	public RocketChatMediator(String authToken, SQLDatabase database, RasaNlu rasa) {
		super(authToken);
		this.database = database;
		String[] auth = authToken.split(":");
		username = auth[0];
		password = auth[1];
		if (activeSubscriptions == null) {
			activeSubscriptions = new HashSet<String>();
		}
		client = new RocketChatAPI(url);
		client.setReconnectionStrategy(new ReconnectionStrategy(4, 2000));
		client.setPingInterval(15000);
		client.connect(this);
		RocketChatAPI.LOGGER.setLevel(Level.OFF);
		this.rasa = rasa;
		messageCollector.setDomain(url);
	}

	public RocketChatMediator(String authToken, SQLDatabase database) {
		super(authToken);
		this.database = database;
		String[] auth = authToken.split(":");
		username = auth[0];
		password = auth[1];
		if (activeSubscriptions == null) {
			activeSubscriptions = new HashSet<String>();
		}
		client = new RocketChatAPI(url);
		client.setReconnectionStrategy(new ReconnectionStrategy(4, 2000));
		client.setPingInterval(15000);
		client.connect(this);
		RocketChatAPI.LOGGER.setLevel(Level.OFF);
		messageCollector.setDomain(url);
	}

	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {

		ChatRoom room = client.getChatRoomFactory().getChatRoomById(channel);
		System.out.println("Sending File Message to : " + room.getRoomData().getRoomId());
		String newText = text.replace("\\n", "\n");
		Client textClient = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
		WebTarget target = textClient.target(url + "/api/v1/rooms.upload/" + room.getRoomData().getRoomId());
		try {
			FileDataBodyPart filePart = new FileDataBodyPart("file", f);
			FormDataMultiPart mp = new FormDataMultiPart();
			FormDataMultiPart multipart = (FormDataMultiPart) mp.field("msg", newText).field("description", "")
					.bodyPart(filePart);
			Response response = target.request().header("X-User-Id", client.getMyUserId()).header("X-Auth-Token", token)
					.post(Entity.entity(multipart, multipart.getMediaType()));
			System.out.println(response.getEntity().toString());
			mp.close();
			multipart.close();
			try {
				java.nio.file.Files.deleteIfExists(Paths.get(f.getName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			try {
				java.nio.file.Files.deleteIfExists(Paths.get(f.getName()));
			} catch (IOException g) {
				// TODO Auto-generated catch block
				g.printStackTrace();
			}
		}
	}

	@Override
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String text, String fileType,
										 Optional<String> id) {
		byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBody);
		File file = new File(fileName + "." + fileType);
		try {
			FileUtils.writeByteArrayToFile(file, decodedBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendFileMessageToChannel(channel, file, text, id);
	}

	@Override
	public void sendMessageToChannel(String channel, String text, Optional<String> id) {
		System.out.println(text);
		ChatRoom room = client.getChatRoomFactory().getChatRoomById(channel);
		System.out.println("Sending Message to : " + room.getRoomData().getRoomId());
		if (sendingMessage.get(channel) != null) {
			while (sendingMessage.get(channel) == true) {

			}
		}
		sendingMessage.put(channel, true);

		room.getMembers(new GetMembersListener() {

			@Override
			public void onGetRoomMembers(Integer arg0, List<UserObject> arg1, ErrorObject arg2) {
				// TODO Auto-generated method stub
				try {
					String userName = "";
					String newText = text;
					for (UserObject u : (ArrayList<UserObject>) arg1) {
						if (!u.getUserId().equals(client.getMyUserId())) {
							userName += u.getUserName() + ", ";
						}
					}

					if (userName.length() > 2) {
						userName = userName.substring(0, userName.length() - 2);
					}
					System.out.println(username + newText);
					newText = newText.replace("menteeName", userName);
					newText = newText.replace("\\n", "\n");
					if (newText.length() > 5000) {
						sendingMessage.put(channel, false);
						try {
							File tempFile = new File("message.txt");
							FileWriter writer = new FileWriter(tempFile);
							writer.write(newText);
							writer.close();
							room.uploadFile(tempFile, "message.txt", "", new FileListener() {

								@Override
								public void onSendFile(RocketChatMessage arg0, ErrorObject arg1) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onUploadError(ErrorObject arg0, IOException arg1) {
									room.sendMessage(arg0.getMessage());
									room.sendMessage(arg0.getReason());
									tempFile.delete();
								}

								@Override
								public void onUploadProgress(int arg0, String arg1, String arg2, String arg3) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onUploadStarted(String arg0, String arg1, String arg2) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onUploadComplete(int arg0, com.rocketchat.core.model.FileObject arg1,
										String arg2, String arg3, String arg4) {
									tempFile.delete();
								}
							});
						} catch (IOException e) {
							// TODO Auto-generated catch block
							sendingMessage.put(channel, false);
							e.printStackTrace();
						}
					} else {
						room.sendMessage(newText);
						sendingMessage.put(channel, false);
					}
				} catch (Exception e) {
					sendingMessage.put(channel, false);
					e.printStackTrace();
				}
				sendingMessage.put(channel, false);
			}

		});

	}

	@Override
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages = this.messageCollector.getMessages();
		return messages;
	}

	@Override
	public String getChannelByEmail(String email) {
		List<UserDocument> users = client.getDbManager().getUserCollection().getData();
		for (UserDocument u : users) {
			// TODO Email Matching
			return u.getName();
		}
		return null;
	}

	@Override
	public void onGetRooms(List<RoomObject> rooms, ErrorObject error) {
		if (error == null) {
			try {
				// System.out.println("Available rooms: " + rooms.size());
				ChatRoomFactory factory = client.getChatRoomFactory();
				synchronized (factory) {
					ArrayList<ChatRoom> roomList = factory.createChatRooms(rooms).getChatRooms();
					for (ChatRoom room : roomList) {
						if (!activeSubscriptions.contains(room.getRoomData().getRoomId())) {
							room.subscribeRoomMessageEvent(new SubscribeListener() {
								@Override
								public void onSubscribe(Boolean isSubscribed, String subId) {

								}
							}, this);
							activeSubscriptions.add(room.getRoomData().getRoomId());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onLogin(TokenObject token, ErrorObject error) {
		if (error == null) {
			System.out.println("Logged in successfully, returned token " + token.getAuthToken());
			client.getRooms(this);
			this.token = token.getAuthToken();
			GetRoomListener grl = this;
			if (checkRooms == null) {
				if (shouldCheckRooms == false) {
					shouldCheckRooms = true;
				}
				checkRooms = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							while (shouldCheckRooms) {
								client.getRooms(grl);
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				System.out.println("Thread created");
				checkRooms.start();
			}

		} else {
			System.out.println("Got error " + error.getMessage());
		}
	}

	@Override
	public void onConnect(String sessionID) {
		System.out.println("Connected to server.");
		client.login(username, password, this);
	}

	@Override
	public void onConnectError(Exception arg0) {
		System.out.println("R.C connection error: " + arg0.getMessage());
		// TODO Auto-generated method stub
		if (checkRooms != null) {
			shouldCheckRooms = false;
			checkRooms = null;
			activeSubscriptions = new HashSet<String>();
			System.out.println("Thread stopped");
		}
	}

	@Override
	public void onDisconnect(boolean arg0) {
		System.out.println("R.C disconnect : " + arg0);
		// TODO Auto-generated method stub
		if (checkRooms != null) {
			shouldCheckRooms = false;
			checkRooms = null;
			activeSubscriptions = new HashSet<String>();
			System.out.println("Thread stopped");
		}
	}

	@Override
	public void onSubscribe(Boolean arg0, String arg1) {
		// System.out.println(arg1);
	}

	public static int countWords(String s) {

		int wordCount = 0;

		boolean word = false;
		int endOfLine = s.length() - 1;

		for (int i = 0; i < s.length(); i++) {
			// if the char is a letter, word = true.
			if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
				word = true;
				// if char isn't a letter and there have been letters before,
				// counter goes up.
			} else if (!Character.isLetter(s.charAt(i)) && word) {
				wordCount++;
				word = false;
				// last word of String; if it doesn't end with a non letter, it
				// wouldn't count without this.
			} else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
				wordCount++;
			}
		}
		return wordCount;
	}

	protected String getTxtFile(String userId, String file) {
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("cookie", "rc_uid=" + userId + "; rc_token=" + token + "; ");
		ClientResponse r = textClient.sendRequest("GET", file, "", MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN,
				textClientHeader);
		InputStream in = new ByteArrayInputStream(r.getRawResponse());
		StringWriter writer = new StringWriter();
		String encoding = StandardCharsets.UTF_8.name();
		try {
			IOUtils.copy(in, writer, encoding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}

	protected String getFileBase64(String userId, String file) {
		System.out.println(userId);
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("cookie", "rc_uid=" + userId + "; rc_token=" + token + "; ");
		ClientResponse r = textClient.sendRequest("GET", file, "", MediaType.TEXT_PLAIN, "application/pdf",
				textClientHeader);
		return Base64.encodeBytes(r.getRawResponse());
	}

	protected byte[] getPDFFile(String userId, String file) {
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("cookie", "rc_uid=" + userId + "; rc_token=" + token + "; ");
		ClientResponse r = textClient.sendRequest("GET", file, "", MediaType.TEXT_PLAIN, "application/pdf",
				textClientHeader);
		return r.getRawResponse();
	}

	protected int getStudentRole(String email) {
		int role = 0;
		PreparedStatement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {

			conn = database.getDataSource().getConnection();
			stmt = conn.prepareStatement("SELECT role FROM users WHERE email=?");
			stmt.setString(1, email);
			rs = stmt.executeQuery();
			while (rs.next())
				role = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
			;
		}
		return role;
	}

	protected String getStudentEmail(String userName) {
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("X-User-Id", client.getMyUserId());
		textClientHeader.put("X-Auth-Token", token);
		ClientResponse r = textClient.sendRequest("GET", "api/v1/users.info?username=" + userName, "",
				MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, textClientHeader);
		System.out.println("respèone is" + r.getResponse());
		JSONObject userObject = new JSONObject(r.getResponse());
		System.out.println("Error now");
		JSONArray emails = userObject.getJSONObject("user").getJSONArray("emails");
		System.out.println("Or not");
		return emails.getJSONObject(0).getString("address");
	}

	@Override
	public void onGetSubscriptions(List<SubscriptionObject> subscriptions, ErrorObject error) {
		// Creating Logical ChatRooms using factory class
	}



	//Leo: sending of file messages is already implemented
	@Override
	public void sendAttachmentMessageToChannel(String channel, String attachments, Optional<String> id){}

	@Override
	public void onMessage(String arg0, RocketChatMessage message) {
		ChatRoom room = client.getChatRoomFactory().getChatRoomById(message.getRoomId());
		synchronized (room) {
			if (!message.getSender().getUserId().equals(client.getMyUserId())) {
				String email = getStudentEmail(message.getSender().getUserName());
				System.out.println("Email: " + email);
				System.out.println("Message: " + message.getMessage());

				Type type = message.getMsgType();
				if (type.equals(Type.ATTACHMENT)) {
					System.out.println("Handling attachement");
					JSONObject j = message.getRawJsonObject();
					String fileType = j.getJSONObject("file").getString("type");
					String fileName = j.getJSONObject("file").getString("name");
					System.out.println(j);
					if (fileType.equals("text/plain") || fileType.equals("application/pdf")
							|| fileType.equals("image/png")) {
						String file = j.getJSONArray("attachments").getJSONObject(0).getString("title_link")
								.substring(1);
						JSONObject bodyJSON = new JSONObject();
						String fileBody = getFileBase64(client.getMyUserId(), file);
						messageCollector.handle(message, fileBody, fileName, fileType, 0,
								getStudentEmail(message.getSender().getUserName()));
					} else {
						messageCollector.handle(message, 0, getStudentEmail(message.getSender().getUserName()));
					}
				} else {
					messageCollector.handle(message, 0, getStudentEmail(message.getSender().getUserName()));
				}
			}
		}
	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, Optional<String> id) {

	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks) {
		super.sendBlocksMessageToChannel(channel, blocks);
	}

	@Override
	public void close() {
		shouldCheckRooms = false;
		checkRooms = null;
		activeSubscriptions = null;
		System.out.println("Thread stopped");
		client.disconnect();
		// client = null;
	}
}
