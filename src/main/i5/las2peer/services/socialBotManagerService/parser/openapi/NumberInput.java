package i5.las2peer.services.socialBotManagerService.parser.openapi;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;

public class NumberInput extends ParameterInput {

    int max;
    int min;
    int multipleOf;
    boolean useMax;
    boolean useMin;
    boolean useMulti;

    @Override
    public boolean validate(String value) {

	if (super.getType() == InputType.Number)
	    return validateInteger(value);

	
	if (super.getType() == InputType.Decimal)
	    return validateDouble(value);

	return super.getType().validate(value);

    }

    public boolean validateInteger(String value) {

	Integer number;

	try {
	    number = Integer.valueOf(value);
	} catch (Exception e) {
	    return false;
	}

	if (useMax) {
	    if (max < number)
		return false;
	}

	if (useMin) {
	    if (min > number)
		return false;
	}

	if (useMulti) {
	    if (number % multipleOf != 0)
		return false;
	}

	return true;
    }

    public boolean validateDouble(String value) {

	Double number;

	try {
	    number = Double.valueOf(value);
	} catch (Exception e) {
	    return false;
	}

	if (useMax) {
	    if (max < number)
		return false;
	}

	if (useMin) {
	    if (min > number)
		return false;
	}

	if (useMulti) {
	    if (number % multipleOf != 0)
		return false;
	}

	return true;
    }

    public int getMax() {
	return max;
    }

    public void setMax(int max) {
	this.useMax = true;
	this.max = max;
    }

    public int getMin() {
	return min;
    }

    public void setMin(int min) {
	this.useMin = true;
	this.min = min;
    }

    public int getMultipleOf() {
	return multipleOf;
    }

    public void setMultipleOf(int multipleOf) {
	this.useMulti = true;
	this.multipleOf = multipleOf;
    }

}
