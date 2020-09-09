package i5.las2peer.services.socialBotManagerService.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class OpenAPIReaderJson {

	public JSONObject read(String url) {

		JSONObject api = null;

		try {
			api = readJsonFromUrl(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(api);
		readComponents(api);
		int version = parseVersion(api);
		System.out.println(version);

		return null;

	}
	
	public List<JSONObject> readComponents(JSONObject api) {
		JSONObject components = (JSONObject) api.get("components");
		JSONObject schemas = (JSONObject) components.get("schemas");
		schemas.keySet().forEach(key ->
		    {
		        Object value = schemas.get(key);
		        System.out.println("key: "+ key + " value: " + value);

		    });
		
		
		return null;
	}
	
	
	private int parseVersion(JSONObject api) {
		String version = api.getAsString("openapi");

		if (version == null)
			System.out.println("no version specified");

		version = version.substring(0, 1);
		int res = 0;
		try {
			res = Integer.parseInt(version);
		} catch (Exception e) {
			System.out.println("cant parse version number");
		}
		return res;
	}

	private JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject json = (JSONObject) p.parse(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}
