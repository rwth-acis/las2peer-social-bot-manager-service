package i5.las2peer.services.socialBotManagerService.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Test {
	
	public static void main(String[] args){
		
		String url = "https://petstore3.swagger.io/api/v3/openapi.json";
		
		
			
			try {
				OpenAPIReader.readAction(url, "/pet", "post");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
}
