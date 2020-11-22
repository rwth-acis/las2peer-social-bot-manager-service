package i5.las2peer.services.socialBotManagerService.parser.training;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class TrainingData {

	@ApiModelProperty(dataType = "string", value = "The name of the nlu module", required = true)
	String nluName;

	List<DataGroup> dataGroup;

	public TrainingData() {
		this.dataGroup = new ArrayList<>();
	}
	
	public String getNluName() {
		return nluName;
	}

	public void setNluName(String nluName) {
		this.nluName = nluName;
	}

	public List<DataGroup> getDataGroup() {
		return dataGroup;
	}

	public void setDataGroup(List<DataGroup> dataGroup) {
		this.dataGroup = dataGroup;
	}

	public Collection<String> intents() {
		
		List<String> res = new ArrayList<>();
		for(DataGroup dg :this.dataGroup) {
			res.add(dg.getIntent());
		}
		return res;
	}
	
	public String toMarkdown() {
		String res = "";
		for(DataGroup dg :this.dataGroup) {
			res = res + "## intent: " + dg.getIntent() + "\n\n";
			for(String example :dg.getExamples()) {
				res = res + "-" + example + "\n\n";
			}
		}
		return res;
	}

	public void fromMarkdown(String data) {
		System.out.println("fromMarkdown");
		data = data.replaceAll("\n", "").replaceAll(" ", "");
		System.out.println(data);
		String[] arr = data.split("##intent:");
		System.out.println(arr.length);
		for(int i = 1; i<arr.length; i++) {
			DataGroup dg = new DataGroup();
			String[] exa = arr[i].split("-");
			dg.setIntent(exa[0]);
			for(int j=1; j<exa.length; j++) {
				dg.addExample(exa[j]);
			}
			this.dataGroup.add(dg);
		}
		
	}

}
