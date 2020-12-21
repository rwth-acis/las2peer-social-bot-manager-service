package i5.las2peer.services.socialBotManagerService.parser.openapi;

public enum ResponseParseMode {
    MESSAGE_TEXT, FUNCTION_TEXT, JSON_TO_MARKDOWN, FILE, VARS;
    
    public static ResponseParseMode fromString(String value) {			
    	
    	switch(value) {
    	case "Plain":
    	case "Text":
    		return MESSAGE_TEXT;
    	case "JSON to Markdown":
    		return JSON_TO_MARKDOWN;
    	case "File":
    		return FILE;
    	case "Vars":
    	case "Edited":
    		return VARS;    	
    	}
    	
    	return MESSAGE_TEXT;    	
    }
}
