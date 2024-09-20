package services.socialBotManagerService.controller;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import services.socialBotManagerService.chat.ChatService;
import services.socialBotManagerService.chat.github.GitHubIssueMediator;
import services.socialBotManagerService.chat.github.GitHubPRMediator;
import services.socialBotManagerService.model.Bot;
import services.socialBotManagerService.model.Messenger;
import services.socialBotManagerService.service.SocialBotManagerService;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collection;

// @Api(value = "GitHub Webhook Receiver Resource")
@Tag(name="GitHub Webhook Receiver Resource", description = "Resource for receiving GitHub webhook events.")
@RestController
@RequestMapping("/github")
public class GitHubWebhookController {

    @Autowired
    private SocialBotManagerService service;
    /**
     * Receives incoming webhook events from a GitHub app and sends them to related GitHub chat mediators.
     *
     * @param body Event
     * @param eventName Name of event
     * @param gitHubAppId Id of GitHub app
     * @return 200
     */
    @POST
    @Path("/webhook/{gitHubAppId}")
    public Response receiveWebhookEvent(String body, @HeaderParam("X-GitHub-Event") String eventName,
                                       @PathParam("gitHubAppId") int gitHubAppId) {
        JSONObject payload = (JSONObject) JSONValue.parse(body);

        // put name of event and payload into one JSONObject
        JSONObject eventObj = new JSONObject();
        eventObj.put("event", eventName);
        eventObj.put("payload", payload);

        // SocialBotManagerService service = new SocialBotManagerService();

        // need to find bot(s) that use this GitHub app id
        Collection<Bot> bots = service.getConfig().getBots().values();
        for (Bot bot : bots) {
            Messenger messenger = bot.getMessenger(ChatService.GITHUB_ISSUES);
            if (messenger != null) {
                GitHubIssueMediator mediator = (GitHubIssueMediator) messenger.getChatMediator();
                if (mediator.getGitHubAppId() == gitHubAppId) {
                    mediator.handleEvent(eventObj);
                }
            }

            messenger = bot.getMessenger(ChatService.GITHUB_PR);
            if (messenger != null) {
                GitHubPRMediator mediator = (GitHubPRMediator) messenger.getChatMediator();
                if (mediator.getGitHubAppId() == gitHubAppId) {
                    mediator.handleEvent(eventObj);
                }
            }
        }

        return Response.status(200).build();
    }
}
