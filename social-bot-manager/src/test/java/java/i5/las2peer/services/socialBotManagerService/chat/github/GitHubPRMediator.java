package i5.las2peer.services.socialBotManagerService.chat.github;

import i5.las2peer.services.socialBotManagerService.chat.AuthTokenException;
import net.minidev.json.JSONObject;

/**
 * Chat mediator for comments on GitHub pull requests.
 */
public class GitHubPRMediator extends GitHubChatMediator {

    /**
     * Constructor for GitHub pull request chat mediators.
     *
     * @param authToken Format: [GitHub app id]:[GitHub app private key in pkcs8]
     * @throws GitHubAppHelper.GitHubAppHelperException If something related to the GitHubAppHelper is not working.
     * @throws AuthTokenException                       If format of {@code authToken} is incorrect.
     */
    public GitHubPRMediator(String authToken) throws GitHubAppHelper.GitHubAppHelperException, AuthTokenException {
        super(authToken);
        this.eventNameItemOpened = "pull_request";
        this.itemNameOpened = "pull_request";
    }

    /**
     * Adds new pull request comment to the message collector (if given event contains one).
     *
     * @param parsedEvent JSON representation of incoming GitHub event
     */
    @Override
    public void handleEvent(JSONObject parsedEvent) {
        if (!this.isRelevantEvent(parsedEvent)) return;

        // check if event belongs to a pull request
        JSONObject payload = (JSONObject) parsedEvent.get("payload");
        // note: in GitHub a pull request also seems to be an issue
        if (payload.containsKey("issue")) {
            // could be a pull request comment
            JSONObject issue = (JSONObject) payload.get("issue");
            if (issue.containsKey("pull_request")) {
                // event belongs to a pull request (and not to an issue)
                super.handleEvent(parsedEvent);
            }
        } else {
            // could be a newly opened pull request
            if (payload.containsKey("pull_request")) {
                super.handleEvent(parsedEvent);
            }
        }
    }
}
