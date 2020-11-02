package i5.las2peer.services.socialBotManagerService.parser.training;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class Training {

    @ApiModelProperty(dataType = "string", value = "The name of the nlu module", required = true)
    String nluName;
    List<DataGroup> dataGroup;

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

}
