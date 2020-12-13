package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import net.minidev.json.JSONObject;

public abstract class Node {

    public abstract boolean isReady();

    public abstract boolean isFull();

    public abstract boolean isConfirmed();

    protected abstract void invariant();

    public abstract NodeList getAll();

    public abstract Node next();

    public abstract JSONObject toBodyJSON();
}
