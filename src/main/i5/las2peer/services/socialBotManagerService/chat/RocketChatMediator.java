package i5.las2peer.services.socialBotManagerService.chat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.rocketchat.common.data.lightdb.document.UserDocument;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.common.network.Socket.State;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.RocketChatAPI.ChatRoom;
import com.rocketchat.core.callback.FileListener;
import com.rocketchat.core.callback.GetSubscriptionListener;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.callback.MessageListener;
import com.rocketchat.core.callback.RoomListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RocketChatMessage.Type;
import com.rocketchat.core.model.RoomObject;
import com.rocketchat.core.model.SubscriptionObject;
import com.rocketchat.core.model.TokenObject;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;

public class RocketChatMediator extends ChatMediator implements ConnectListener, LoginListener,
		RoomListener.GetRoomListener, SubscribeListener, GetSubscriptionListener {

	private final static String url = "https://chat.tech4comp.dbis.rwth-aachen.de";
	RocketChatAPI client;
	private String username = "las2peer";
	private String password;
	private String token;
	private RocketChatMessageCollector messageCollector = new RocketChatMessageCollector();

	public RocketChatMediator(String authToken) {
		super(authToken);
		password = authToken;
		client = new RocketChatAPI(url);
		client.setReconnectionStrategy(new ReconnectionStrategy(4, 2000));
		client.setPingInterval(15000);
		client.connect(this);
	}

	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		ChatRoom room = client.getChatRoomFactory().getChatRoomById(channel);
		room.subscribeRoomMessageEvent(new SubscribeListener() {
			@Override
			public void onSubscribe(Boolean isSubscribed, String subId) {

			}
		}, new MessageListener.SubscriptionListener() {
			@Override
			public void onMessage(String arg0, RocketChatMessage arg1) {
				// TODO Auto-generated method stub

			}
		});
		// CHECK VARS

		room.sendMessage(text);
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
			for (RoomObject room : rooms) {
				System.out.println("Room name is " + room.getRoomName());
				System.out.println("Room id is " + room.getRoomId());
				System.out.println("Room topic is " + room.getTopic());
				System.out.println("Room type is " + room.getRoomType());
			}
		} else {
			System.out.println("Got error " + error.getMessage());
		}
	}

	@Override
	public void onLogin(TokenObject token, ErrorObject error) {
		if (error == null) {
			System.out.println("Logged in successfully, returned token " + token.getAuthToken());
			client.getRooms(this);
			this.token = token.getAuthToken();
			GetSubscriptionListener subscriptionListener = this;
			SubscribeListener subscribeListener = this;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (client.getState().equals(State.CONNECTED)) {
							client.subscribeActiveUsers(subscribeListener);
							client.getSubscriptions(subscriptionListener);
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSubscribe(Boolean arg0, String arg1) {
		System.out.println(arg1);
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

	@Override
	public void onGetSubscriptions(List<SubscriptionObject> subscriptions, ErrorObject error) {
		// Creating Logical ChatRooms using factory class
		ChatRoomFactory factory = client.getChatRoomFactory();
		synchronized (factory) {
			for (ChatRoom c : factory.getChatRooms()) {
				c.unSubscribeAllEvents();
				factory.removeChatRoomById(c.getRoomData().getRoomId());
			}
			ArrayList<ChatRoom> roomList = factory.createChatRooms(subscriptions).getChatRooms();

			for (ChatRoom room : roomList) {
				room.subscribeRoomMessageEvent(new SubscribeListener() {
					@Override
					public void onSubscribe(Boolean isSubscribed, String subId) {

					}
				}, new MessageListener.SubscriptionListener() {
					@Override
					public void onMessage(String arg0, RocketChatMessage message) {
						if (!message.getSender().getUserId().equals(client.getMyUserId())) {
							Type type = message.getMsgType();
							if (type.equals(Type.ATTACHMENT)) {
								try {
									new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												System.out.println("Handling attachement");
												JSONObject j = message.getRawJsonObject();
												String fileType = j.getJSONObject("file").getString("type");
												String fileName = j.getJSONObject("file").getString("name");
												if (fileType.equals("text/plain")) {
													room.sendMessage(
															"Ich analysiere gerade deinen Text. Das kann einen Moment dauern. 👨‍🏫");
													MiniClient textClient = new MiniClient();
													textClient.setConnectorEndpoint(url);
													HashMap<String, String> textClientHeader = new HashMap<String, String>();
													textClientHeader.put("cookie", "rc_uid=" + client.getMyUserId()
															+ "; rc_token=" + token + "; ");
													ClientResponse r = textClient.sendRequest("GET",
															j.getJSONArray("attachments").getJSONObject(0)
																	.getString("title_link").substring(1),
															"", MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN,
															textClientHeader);
													String body = r.getResponse();
													int numWords = countWords(body);
													if (numWords < 350) {
														room.sendMessage(
																"Der Text muss mindestens 350 Woerter enthalten (aktuell: "
																		+ numWords + ").");
													} else {
														MiniClient c = new MiniClient();
														c.setConnectorEndpoint(
																"https://las2peer.tech4comp.dbis.rwth-aachen.de");
														HashMap<String, String> headers = new HashMap<String, String>();
														// TODO
														ClientResponse result = c.sendRequest("POST",
																"tmitocar/" + message.getRoomId() + "/", body,
																MediaType.TEXT_PLAIN, "image/png", headers);
														System.out.println("Submitted text: " + result.getHttpCode());
														InputStream in = new ByteArrayInputStream(
																result.getRawResponse());
														BufferedImage bImageFromConvert = ImageIO.read(in);
														File tempFile = new File(message.getRoomId() + ".png");
														ImageIO.write(bImageFromConvert, "png", tempFile);
														room.uploadFile(tempFile, fileName + ".png", "",
																new FileListener() {

																	@Override
																	public void onSendFile(RocketChatMessage arg0,
																			ErrorObject arg1) {
																		// TODO Auto-generated method stub
																	}

																	@Override
																	public void onUploadError(ErrorObject arg0,
																			IOException arg1) {
																		room.sendMessage(arg0.getMessage());
																		room.sendMessage(arg0.getReason());
																		tempFile.delete();
																	}

																	@Override
																	public void onUploadProgress(int arg0, String arg1,
																			String arg2, String arg3) {
																		// TODO Auto-generated method stub

																	}

																	@Override
																	public void onUploadStarted(String arg0,
																			String arg1, String arg2) {
																		// TODO Auto-generated method stub

																	}

																	@Override
																	public void onUploadComplete(int arg0,
																			com.rocketchat.core.model.FileObject arg1,
																			String arg2, String arg3, String arg4) {
																		room.sendMessage(
																				"Hier ist deine Wissenslandkarte:");
																		tempFile.delete();

																	}
																});
													}
												} else {
													room.sendMessage("Der Typ `" + fileType
															+ "` wird momentan nicht unterstuetzt.");
												}
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
											System.out.println("Intent processing finished.");
										}
									}).start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								messageCollector.handle(message);
							}
						}
					}
				});
			}
		}
	}
}
