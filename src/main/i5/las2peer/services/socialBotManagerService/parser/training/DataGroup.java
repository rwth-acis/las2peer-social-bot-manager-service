package i5.las2peer.services.socialBotManagerService.parser.training;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class DataGroup {

	@ApiModelProperty(dataType = "string", value = "The intent", required = true)
	String intent;
	@ApiModelProperty(value = "example of an utterance", required = true)
	List<String> examples;

	DataGroup() {
		this.examples = new ArrayList<>();
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
		
	public String toMarkdown() {
		String res = "## intent: ";
		for(String example :examples) {
			res = res + "\n\n- " + example;
		}
		return res;
	}

}
