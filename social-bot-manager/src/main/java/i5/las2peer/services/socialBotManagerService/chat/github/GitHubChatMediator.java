package i5.las2peer.services.socialBotManagerService.chat.github;

import i5.las2peer.services.socialBotManagerService.chat.AuthTokenException;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessageCollector;
import i5.las2peer.services.socialBotManagerService.chat.EventChatMediator;
import net.minidev.json.JSONObject;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * Parent class for GitHub issue and pull request chat mediators.
 * <p>
 * In the GitHubChatMediator, a channel is a comment section of an issue or a pull request.
 * Therefore, the "messenger channel" is defined as follows:
 * [Owner name]/[Repo name]#[Issue or PR number]
 */
public abstract class GitHubChatMediator extends EventChatMediator {

    /**
     * Message collector: New comments are added to the collector in {@link #handleEvent(JSONObject) handleEvent}.
     */
    private ChatMessageCollector messageCollector;

    /**
     * Helper for the GitHub app that is used by the chat mediator.
     */
    private GitHubAppHelper gitHubAppHelper;

    /**
     * Id of the GitHub app that is used by the chat mediator.
     */
    private int gitHubAppId;

    /**
     * Event name for new comments is the same for both issues and pull requests.
     */
    private final String eventNameItemComment = "issue_comment";

    /**
     * The action name for new comments is the same for both issues and pull requests.
     */
    private final String actionNameItemComment = "created";

    /**
     * Event name for a newly opened issue or pull request.
     */
    protected String eventNameItemOpened;

    /**
     * Action name for a newly opened issue or pull request.
     */
    private final String actionNameItemOpened = "opened";

    /**
     * Name of the field that contains the comment information (same for issues and pull requests).
     */
    private final String itemNameComment = "issue";

    /**
     * Name of the field that contains the text of a newly opened issue or pull request.
     */
    protected String itemNameOpened;

    /**
     * Constructor for GitHub chat mediators.
     *
     * @param authToken Format: [GitHub app id]:[GitHub app private key in pkcs8]
     * @throws GitHubAppHelper.GitHubAppHelperException If something related to the GitHubAppHelper is not working.
     * @throws AuthTokenException                       If format of {@code authToken} is incorrect.
     */
    public GitHubChatMediator(String authToken) throws GitHubAppHelper.GitHubAppHelperException, AuthTokenException {
        super(authToken);

        // use default message collector
        this.messageCollector = new ChatMessageCollector();

        // check that authToken contains app id and private key
        String[] parts = authToken.split(":");
        if (parts.length != 2) {
            throw new AuthTokenException("Incorrect auth information, format should be: " +
                    "[GitHub app id]:[GitHub app private key in pkcs8]");
        }

        // get app id and private key
        this.gitHubAppId = Integer.parseInt(parts[0]);
        String pkcs8PrivateKey = parts[1];

        // init GitHub app helper
        this.gitHubAppHelper = new GitHubAppHelper(this.gitHubAppId, pkcs8PrivateKey);
    }

    /**
     * Used to filter out events that are not relevant for the chat mediators.
     *
     * @param parsedEvent Event
     * @return Whether the given event is relevant for the chat mediators.
     */
    protected boolean isRelevantEvent(JSONObject parsedEvent) {
        String event = parsedEvent.getAsString("event");
        return List.of(eventNameItemComment, eventNameItemOpened).contains(event);
    }

    /**
     * Adds new comment to {@link GitHubChatMediator#messageCollector messageCollector} (if given event contains one).
     *
     * @param parsedEvent JSON representation of incoming GitHub event
     */
    @Override
    public void handleEvent(JSONObject parsedEvent) {
        // extract name and payload of given event
        String eventName = parsedEvent.getAsString("event");
        JSONObject payload = (JSONObject) parsedEvent.get("payload");

        String repositoryFullName = this.getRepositoryFullNameOfEvent(parsedEvent);
        String action = payload.getAsString("action");

        boolean itemComment = eventName.equals(eventNameItemComment) && action.equals(actionNameItemComment);
        boolean itemOpened = eventName.equals(eventNameItemOpened) && action.equals(actionNameItemOpened);

        if (itemComment || itemOpened) {
            String itemName = itemComment ? itemNameComment : itemNameOpened;
            JSONObject item = (JSONObject) payload.get(itemName);
            String channelName = repositoryFullName + "#" + item.getAsNumber("number");

            JSONObject comment;
            if (itemComment) comment = (JSONObject) payload.get("comment");
            else if (itemOpened) comment = (JSONObject) payload.get(itemName);
            else return;

            // extract user info from comment
            JSONObject user = (JSONObject) comment.get("user");
            String username = user.getAsString("login");
            String message = comment.getAsString("body");

            // dont handle bot messages
            if (this.isBotAccount(user)) return;

            // add comment to message collector
            ChatMessage chatMessage = new ChatMessage(channelName, username, message);
            this.messageCollector.addMessage(chatMessage);
        }
    }

    /**
     * Comments on an issue or pull request. As in GitHub a pull request also seems to be an issue, this method can
     * be shared for both chat mediators.
     *
     * @param channel Format: [Owner name]/[Repo name]#[Issue or PR number]
     * @param text    The content of the comment
     * @param id
     */
    @Override
    public void sendMessageToChannel(String channel, String text, Optional<String> id) {
        String repositoryFullName = channel.split("#")[0];
        int number = Integer.parseInt(channel.split("#")[1]);

        try {
            GitHub instance = this.gitHubAppHelper.getGitHubInstance(repositoryFullName);
            if (instance != null) {
                // post comment (in GitHub a pull request also seems to be an issue)
                instance.getRepository(repositoryFullName).getIssue(number).comment(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Vector<ChatMessage> getMessages() {
        return this.messageCollector.getMessages();
    }

    /**
     * Returns the id of the GitHub app that the chat mediator is using.
     *
     * @return Id of the GitHub app that the chat mediator is using.
     */
    public int getGitHubAppId() {
        return this.gitHubAppId;
    }

    /**
     * Extracts the full repository name from an event JSONObject.
     *
     * @param parsedEvent Event
     * @return Full name of the repository, containing both owner and repository name.
     */
    private String getRepositoryFullNameOfEvent(JSONObject parsedEvent) {
        JSONObject payload = (JSONObject) parsedEvent.get("payload");
        JSONObject repository = (JSONObject) payload.get("repository");
        return repository.getAsString("full_name");
    }

    /**
     * Checks if the given user (from GitHub) is a bot.
     *
     * @param user User JSONObject from GitHub
     * @return Whether the given user is a bot.
     */
    private boolean isBotAccount(JSONObject user) {
        return user.getAsString("type").equals("Bot");
    }

    @Override
    public void editMessage(String channel, String messageId, String message, Optional<String> id) {
    }

    @Override
    public void sendBlocksMessageToChannel(String channel, String blocks, String authToken, Optional<String> id) {
    }

    @Override
    public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts, Optional<String> id) {
    }

    @Override
    public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {
    }

    @Override
    public String getChannelByEmail(String email) {
        return null;
    }

    @Override
    public void close() {
    }
}
