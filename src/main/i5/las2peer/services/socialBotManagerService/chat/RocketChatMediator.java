package i5.las2peer.services.socialBotManagerService.chat;

import java.util.List;
import java.util.OptionalLong;
import java.util.Vector;

import com.rocketchat.common.data.lightdb.document.UserDocument;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.RocketChatAPI.ChatRoom;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.callback.MessageListener;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.TokenObject;

public class RocketChatMediator extends ChatMediator {

	private final static String url = "chat.tech4comp.dbis.rwth-aachen.de";
	RocketChatAPI client;
	private String username;
	private String password;

	public RocketChatMediator(String authToken) {
		super(authToken);
		client = new RocketChatAPI("url");
		client.setReconnectionStrategy(new ReconnectionStrategy(4, 2000));
		client.setPingInterval(15000);

	}

	SubscribeListener subscribeListener = new SubscribeListener() {
		@Override
		public void onSubscribe(Boolean isSubscribed, String subId) {
			if (isSubscribed) {
				System.out.println("Subscribed for getting active user statuses");
			}
		}

	};
	LoginListener loginCallback = new LoginListener() {

		@Override
		public void onLogin(TokenObject arg0, ErrorObject arg1) {
			System.out.println("Login is successful");
			client.subscribeActiveUsers(subscribeListener);
		}

	};

	ConnectListener connectListener = new ConnectListener() {
		@Override
		public void onConnect(String sessionID) {
			System.out.println("Connected to server");
			client.login(username, password, loginCallback);
		}

		@Override
		public void onDisconnect(boolean closedByServer) {
			System.out.println("Disconnect detected here");
		}

		@Override
		public void onConnectError(Exception arg0) {
			System.out.println("Got connect error here");
		}
	};

	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {
		ChatRoom room = client.getChatRoomFactory().getChatRoomByName(channel);
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
		room.sendMessage(text);
	}

	@Override
	public Vector<ChatMessage> getMessages() {
		// TODO Auto-generated method stub
		return null;
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

}
