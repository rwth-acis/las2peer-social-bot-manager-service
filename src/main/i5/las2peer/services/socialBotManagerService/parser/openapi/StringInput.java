package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInput extends ParameterInput {

    Integer minLength;
    Integer maxLength;
    Pattern pattern;

    @Override
    public boolean validate(String value) {

	if (this.minLength != null) {
	    if (value.length() < this.minLength)
		return false;
	}

	if (this.maxLength != null) {
	    if (value.length() > this.maxLength)
		return false;
	}

	if (this.pattern != null) {
	    Matcher m = this.pattern.matcher(value);
	    if (!m.matches())
		return false;
	}

	return super.getType().validate(value);
    }

    public Integer getMinLength() {
	return minLength;
    }

    public void setMinLength(int minLength) {
	this.minLength = minLength;
    }

    public Integer getMaxLength() {
	return maxLength;
    }

    public void setMaxLength(int maxLength) {
	this.maxLength = maxLength;
    }

    public Pattern getPattern() {
	return pattern;
    }

    public void setPattern(String pattern) {
	this.pattern = Pattern.compile(pattern);
    }

    public void setPattern(Pattern pattern) {
	this.pattern = pattern;
    }

}
