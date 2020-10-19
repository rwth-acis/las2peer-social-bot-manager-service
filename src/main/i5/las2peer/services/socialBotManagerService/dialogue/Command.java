package i5.las2peer.services.socialBotManagerService.dialogue;

public class Command {

    public String name;
    public String intent;
    public String description;

    public Command() {

    }

    public Command(String name, String intent, String description) {
	super();
	this.name = name;
	this.intent = intent;
	this.description = description;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getIntent() {
	return intent;
    }

    public void setIntent(String intent) {
	this.intent = intent;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void invariant() {
	assert this.intent != null : "command has no intent";
	assert this.description != null : "command description is null";
	assert this.name != null : "command has no name";
	assert !this.description.contentEquals("") : "command has no description";
    }

}
