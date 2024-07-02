package services.socialBotManagerService.chat.github;

import net.minidev.json.JSONObject;
import services.socialBotManagerService.chat.AuthTokenException;

/**
 * Chat mediator for comments on GitHub issues.
 */
public class GitHubIssueMediator extends GitHubChatMediator {

    /**
     * Constructor for GitHub issue chat mediators.
     *
     * @param authToken Format: [GitHub app id]:[GitHub app private key in pkcs8]
     * @throws GitHubAppHelper.GitHubAppHelperException If something related to the GitHubAppHelper is not working.
     * @throws AuthTokenException                       If format of {@code authToken} is incorrect.
     */
    public GitHubIssueMediator(String authToken) throws GitHubAppHelper.GitHubAppHelperException, AuthTokenException {
        super(authToken);
        this.eventNameItemOpened = "issues";
        this.itemNameOpened = "issue";
    }

    /**
     * Adds new issue comment to the message collector (if given event contains one).
     *
     * @param parsedEvent JSON representation of incoming GitHub event
     */
    @Override
    public void handleEvent(JSONObject parsedEvent) {
        if (!this.isRelevantEvent(parsedEvent)) return;

        // check if event belongs to an issue
        JSONObject payload = (JSONObject) parsedEvent.get("payload");
        JSONObject issue = (JSONObject) payload.get("issue");
        if (!issue.containsKey("pull_request")) {
            super.handleEvent(parsedEvent);
        }
    }
}
