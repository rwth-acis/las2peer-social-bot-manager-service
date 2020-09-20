package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;

public abstract class AbstractLanguageGenerator {

    abstract public String parse(DialogueAct act);
    
}
