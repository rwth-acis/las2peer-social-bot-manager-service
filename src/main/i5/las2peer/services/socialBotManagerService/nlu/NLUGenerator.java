package i5.las2peer.services.socialBotManagerService.nlu;

import i5.las2peer.services.socialBotManagerService.model.NLUKnowledge;

public class NLUGenerator {

	public static RasaNlu createRasaNLU(NLUKnowledge nlu) {

		assert nlu != null : "nlu is null";
		assert nlu.getUrl() != null : "nlu has no url";

		RasaNlu rasa = new RasaNlu(nlu.getUrl().toString());
		String name = nlu.getName();
		if (name == null || name.contentEquals(""))
			name = getUrlName(nlu.getUrl().toString());
		rasa.setName(name);

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
