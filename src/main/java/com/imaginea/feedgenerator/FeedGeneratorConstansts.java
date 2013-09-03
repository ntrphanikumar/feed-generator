package com.imaginea.feedgenerator;

public class FeedGeneratorConstansts {
	
	public final static String LAST_BBYOPEN_UPDATES_FILE = "/staging/bestbuy/config/last_bbyopen_update_per_sku.txt";
	public final static String DOWNLOAD_DIR = "/staging/bestbuy/download_bbyopen";
	public final static String OUTPUT_DIR = "/tmp/bestbuy_import_feeds/";
	public final static String API_KEY = "apiKey=nutkuaxtgwur8magkqpca2su";
	public final static String JSON_FORMAT = "format=json";
	public final static String ITEM_TYPES = "hardgood,software,game,BlackTie";
	
	public final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public final static String PRODUCT_ATTR_FILTERS = "marketplace=*&active=true&preowned=*&digital=*";
	public final static int OVER_LAP_TIME_FOR_DATA_FEED=14400000;



}
