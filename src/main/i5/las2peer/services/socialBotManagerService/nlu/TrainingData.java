package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

public class TrainingData {

	@ApiModelProperty(dataType = "string", value = "The name of the nlu module", required = true)
	String moduleName;

	@ApiModelProperty(required = true)
	List<TrainingDataEntry> entries;

	public TrainingData() {
		this.entries = new ArrayList<>();
	}

	public TrainingData(TrainingDataEntry entry) {
		this();
		if (entry.validate())
			this.entries.add(entry);
	}

	public TrainingData(Collection<TrainingDataEntry> entries) {
		this();
		for (TrainingDataEntry entry : entries) {
			if (entry.validate())
				this.addEntry(entry);
		}
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String nluName) {
		this.moduleName = nluName;
	}

	public List<TrainingDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TrainingDataEntry> dataGroup) {
		this.entries = dataGroup;
	}

	public Collection<String> intents() {

		List<String> res = new ArrayList<>();
		for (TrainingDataEntry dg : this.entries) {
			res.add(dg.getIntent());
		}
		return res;
	}

	public void addAll(TrainingData data) {
		for (TrainingDataEntry dg : data.getEntries()) {
			this.addEntry(dg);
		}
	}

	public void addEntry(TrainingDataEntry entry) {

		assert entry != null : "addEntry TrainingDataEntry is null";
		if (entry.getExamples().isEmpty())
			return;

		System.out.println("nlu add entry " + entry.getIntent());
		String intent = entry.getIntent();
		for (TrainingDataEntry tg : this.entries) {
			if (tg.getIntent().contentEquals(intent)) {
				tg.addExamples(entry.getExamples());
				return;
			}
		}

		if (entry.validate())
			this.entries.add(entry);

	}

	@JsonIgnore
	public Collection<String> getIntents() {
		Collection<String> res = new HashSet<>();
		for (TrainingDataEntry entry : this.entries) {
			if (entry.getIntent() != null)
				res.add(entry.getIntent());
		}
		return res;
	}

	public int size() {
		return this.intents().size();
	}

	public String toMarkdown() {
		String res = "";
		for (TrainingDataEntry dg : this.entries) {
			res = res + "## intent: " + dg.getIntent() + "\n\n";
			for (String example : dg.getExamples()) {
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
		for (int i = 1; i < arr.length; i++) {
			TrainingDataEntry dg = new TrainingDataEntry();
			String[] exa = arr[i].split("-");
			dg.setIntent(exa[0]);
			for (int j = 1; j < exa.length; j++) {
				dg.addExample(exa[j]);
			}
			this.entries.add(dg);
		}

	}

}
