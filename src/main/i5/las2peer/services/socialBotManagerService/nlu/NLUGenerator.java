package i5.las2peer.services.socialBotManagerService.nlu;

import i5.las2peer.services.socialBotManagerService.model.NLUKnowledge;

public class NLUGenerator {

	public static RasaNLU createRasaNLU(NLUKnowledge nlu) {

		assert nlu != null : "nlu is null";
		assert nlu.getUrl() != null : "nlu has no url";
		
		String name = nlu.getName();
		RasaNLU rasa = new RasaNLU(name, nlu.getUrl().toString());
		if (name == null || name.contentEquals(""))
			name = getUrlName(nlu.getUrl().toString());

		return rasa;
	}

	public static String getUrlName(String url) {

		assert url != null : "url is null";
		assert (url.startsWith("http://") || url.startsWith("https://")) : "no valid url";

		String prefix = "http://";
		if (url != null && prefix != null && url.startsWith(prefix)) {
			return url.substring(prefix.length());
		} else {
			prefix = "https://";
			if (url != null && prefix != null && url.startsWith(prefix)) {
				return url.substring(prefix.length());
			}
		}
		return url;
	}

}
