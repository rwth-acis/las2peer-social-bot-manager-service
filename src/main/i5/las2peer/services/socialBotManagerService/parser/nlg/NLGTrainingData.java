package i5.las2peer.services.socialBotManagerService.parser.nlg;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class NLGTrainingData {

	@ApiModelProperty(required = true)
	List<NLGDataGroup> datagroups;

	public List<NLGDataGroup> getDatagroups() {
		return datagroups;
	}

	public void setDatagroups(List<NLGDataGroup> datagroups) {
		this.datagroups = datagroups;
	}
	
}
