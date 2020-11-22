package i5.las2peer.services.socialBotManagerService;

import i5.las2peer.services.socialBotManagerService.nlu.TrainingHelper;

public class Test {

	public static void main(String[] args) {
		
		String url = "http://127.0.0.1:5005";
		String config = null;
		String md = "## intent: greet\n\n-hioh\n\n-huhu\n\n";
		String markdown = "## intent:greet\n\n-Hallo\n\n-Moin\n\n-hallo\n\n";
		TrainingHelper helper = new TrainingHelper(url, config, md);
		helper.setDefaultConfig();
		helper.run();	

	}

}
