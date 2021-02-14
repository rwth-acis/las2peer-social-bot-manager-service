package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class TrainingDataEntry {

	public static final int MINSIZE = 3;

	@ApiModelProperty(dataType = "string", value = "The intent", required = true)
	@Pattern(regexp="[a-zA-Z_]*")
	String intent;

	@Size(min = MINSIZE)
	@ApiModelProperty(value = "example of an utterance", required = true)
	List<String> examples;

	public TrainingDataEntry() {
		this.examples = new ArrayList<>();
	}

	public TrainingDataEntry(String intentKeyword, List<String> responseMessages) {
		this();
		this.intent = intentKeyword;
		this.examples = responseMessages;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public List<String> getExamples() {
		return examples;
	}

	public void setExamples(List<String> examples) {
		this.examples = examples;
	}

	public void addExample(String example) {
		this.examples.add(example);
	}

	public void addExamples(List<String> examples) {
		assert examples != null;
		for (String example : examples)
			this.addExample(example);
	}

	public boolean validate() {
		return this.validate(MINSIZE);
	}

	public boolean validate(int minsize) {

		if (this.examples == null) {
			System.out.println("validation failed: no examples");
			return false;
		}

		if (this.examples.size() < minsize) {
			System.out.println("validation failed: not enough examples");
			return false;
		}

		if (this.intent == null) {
			System.out.println("validation failed: no intent");
			return false;
		}

		if (this.intent.length() < 2) {
			System.out.println("validation failed: intent to short");
			return false;
		}

		return true;
	}

	public String toMarkdown() {
		String res = "## intent: ";
		for (String example : examples) {
			res = res + "\n\n- " + example;
		}
		return res;
	}

}
