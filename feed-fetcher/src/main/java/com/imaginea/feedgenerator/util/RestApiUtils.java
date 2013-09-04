package com.imaginea.feedgenerator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RestApiUtils {
	private final static Logger log = Logger.getLogger(RestApiUtils.class);
	private final static JSONParser JSON_PARSER = new JSONParser();
	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
	private final static  int NUM_RETRIES = 3;

	public static JSONObject getJSONResponse(String itemDeltaURL) throws ParseException {
		String response = null;
		log.debug("Excectuing URL :" + itemDeltaURL);
		for (int i = 0; i < NUM_RETRIES; i++) {
			try {
				response = executeGet(itemDeltaURL);
				break;
			} catch (IOException e) {
				log.warn(String.format("Attempting %s times ", (i + 1)));
			}
		}
		JSONObject jsonObject = (JSONObject) JSON_PARSER.parse(response);
		return jsonObject;
	}

	public static String executeGet(String urlStr) throws IOException {
		BufferedReader reader = null;
		StringBuilder responseStr = new StringBuilder();
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				responseStr.append(line);
				responseStr.append("\n");
			}
		} catch (MalformedURLException exception) {
			log.error("MalFormedURL Exception checku url - " + conn.getResponseCode());
			throw exception;
		} catch (IOException exception) {
			log.error("Got Responce Code -" + conn.getResponseCode());
			throw exception;
		} finally {
			if (null != reader)
				reader.close();
		}
		return responseStr.toString();
	}

	public static String convertDateToStringFormat(Date lastRunTimeOfDataFeed, String format) throws java.text.ParseException {
		simpleDateFormat.applyPattern(format);
		return simpleDateFormat.format(lastRunTimeOfDataFeed);
	}

	public static Date convertStringToDateFormat(String dateStr, String format) throws java.text.ParseException {
		simpleDateFormat.applyPattern(format);
		return simpleDateFormat.parse(dateStr);
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				for (File innerFile : file.listFiles()) {
					delete(innerFile);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			file.delete();
		}
	}

}
