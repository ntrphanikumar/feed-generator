package com.imaginea.feedgenerator.process;

import static au.com.bytecode.opencsv.CSVWriter.NO_ESCAPE_CHARACTER;
import static au.com.bytecode.opencsv.CSVWriter.NO_QUOTE_CHARACTER;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DOWNLOAD_DIR;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.OUTPUT_DIR;
import static com.imaginea.feedgenerator.util.Utils.delete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.com.bytecode.opencsv.CSVWriter;

import com.imaginea.feedgenerator.FeedStatus;

public class WriteProductsDataProcessor implements Processor {

	private static final char SEPARATOR = '\t';
	private static final String UTF_8 = "UTF-8";
	private static final Logger log = Logger.getLogger(WriteProductsDataProcessor.class);

	private final String[] RELATION_TYPES = { "psp_prp_products" };
	private final Map<Long, List<String>> INVALID_PRODUCTS = new HashMap<Long, List<String>>();
	private final JSONParser jsonParser;
	private final DecimalFormat decimalFormat;
	private String dataFeedGeneratorHomeDir;

	public WriteProductsDataProcessor() {
		this.jsonParser = new JSONParser();
		this.decimalFormat = new DecimalFormat("#0.00");
	}

	@Override
	public void process(FeedStatus feedStatus) throws Exception {

		dataFeedGeneratorHomeDir = feedStatus.getFeedGeneratorHome();
		log.info("Generating ICS catalog feed files");
		Map<String, File> relationFiles = new HashMap<String, File>();
		File outPutDir = new File(dataFeedGeneratorHomeDir + OUTPUT_DIR);
		File downLoadDir = new File(dataFeedGeneratorHomeDir + DOWNLOAD_DIR);
		if (outPutDir.exists()) {
			delete(outPutDir);
		}
		outPutDir.mkdirs();
		int totalSKUProcessed = 0;
		File productsFile = new File(outPutDir, "bestbuy.products.en-us.txt");
		File attributesFile = new File(outPutDir, "bestbuy.attributes.en-us.txt");
		for (String relationType : RELATION_TYPES) {
			File relationFile = new File(outPutDir, "bestbuy.relations." + relationType + ".txt");
			relationFiles.put(relationType, relationFile);
			CSVWriter relationFileWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(relationFile), UTF_8), '\t', NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER);
			try {
				relationFileWriter.writeNext(new String[] { "primary_seller_product_id", "secondary_seller_product_id", "score" });
			}
			finally{
				IOUtils.closeQuietly(relationFileWriter);
			}
		}
		CSVWriter productsCSVWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(productsFile), UTF_8), SEPARATOR, NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER); CSVWriter attributesCSVWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(attributesFile), UTF_8), SEPARATOR, NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER);
		try{
			productsCSVWriter.writeNext(new String[] { "seller_product_id", "seller_category_id", "seller_product_name", "seller_product_url", "image_for_cross_sell_url", "syndicator_product_id", "syndicator_id" });
			attributesCSVWriter.writeNext(new String[] { "seller_product_id", "attribute_name", "attribute_value", "unit_value" });

			File[] listFiles = downLoadDir.listFiles();
			Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
			log.info(String.format("Beginning processing of %s product files", listFiles.length));
			Map<String, String[]> categoryProductMap = new HashMap<String, String[]>();
			for (File file : listFiles) {
				int errors = 0;
				JSONObject product;
				try{
					product = (JSONObject) jsonParser.parse(StringEscapeUtils.unescapeHtml(FileUtils.readFileToString(file)));
					//product = (JSONObject) jsonParser.parse(new InputStreamReader(new FileInputStream(file),"UTF-8"));
					if (validProduct(product)) {
						categoryProductMap.putAll(getProductCategories(product));
						try {
							writeToAttributeFile(product, attributesCSVWriter);
							writeToProductFile(product, productsCSVWriter);
							writeToRelationsFils(product, relationFiles);
						} catch (Exception exception) {
							exception.printStackTrace();
							log.fatal("Error processing SKU  " + product.get("sku"));
							log.fatal(exception.getMessage());
						}
						totalSKUProcessed++;

					}
				} catch (ParseException exception) {
					log.warn(String.format("error loading file %s", file.getName()));
					errors++;
					if (errors >= 10) {
						log.fatal("Experience too many errors loading files...exiting");
						throw new Exception("Experience too many errors loading files...exiting", exception);
					}
				}
			}
			log.debug(String.format("Successfully processed %s of %s product files", totalSKUProcessed, listFiles.length));
			generateCategoryFiles(categoryProductMap);
			log.info("Invalid Products Found : " + INVALID_PRODUCTS);
		}finally{
			IOUtils.closeQuietly(productsCSVWriter);
		}

	}

	private boolean validProduct(JSONObject product) {
		boolean isValid = true;
		Long sku = (Long) product.get("sku");
		List<String> validationFailedMessagesList = new ArrayList<String>();
		if (((JSONArray) product.get("categoryPath")).size() <= 1) {
			isValid = false;
			validationFailedMessagesList.add("Missing Category Path");
		}

		if (((String) product.get("thumbnailImage")) == null) {
			isValid = false;
			validationFailedMessagesList.add("No 'thumbnailImage'");
		}

		if (!product.containsKey("url")) {
			isValid = false;
			validationFailedMessagesList.add("No 'url'");
		}
		if (!product.containsKey("regularPrice")) {
			isValid = false;
			validationFailedMessagesList.add("Empty string value for regularPrice");
		}

		if (!product.containsKey("salePrice")) {
			isValid = false;
			validationFailedMessagesList.add("Empty string value for salePrice");
		}

		if (!isValid) {
			INVALID_PRODUCTS.put(sku, validationFailedMessagesList);
		}

		return isValid;
	}

	private void generateCategoryFiles(Map<String, String[]> categoryProdutMap) throws Exception {
		File categoryFile = new File(dataFeedGeneratorHomeDir + OUTPUT_DIR, "bestbuy.categories.en-us.txt");
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(categoryFile), UTF_8), SEPARATOR, NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER);
		try {
			csvWriter.writeNext(new String[] { "category_id", "category_name", "parent_category_id" });
			for (Map.Entry<String, String[]> category : categoryProdutMap.entrySet())
				csvWriter.writeNext(category.getValue());
		}finally{
			IOUtils.closeQuietly(csvWriter);
		}
	}

	private void writeToRelationsFils(JSONObject product, Map<String, File> relationFiles) throws Exception {
		int score = 0;
		if (product.containsKey("protectionPlans")) {
			Iterator<JSONObject> protectionPlansIterator = ((JSONArray) product.get("protectionPlans")).iterator();
			while (protectionPlansIterator.hasNext()) {
				CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(relationFiles.get("psp_prp_products"),true), UTF_8), SEPARATOR, NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER);
				try  {
					JSONObject protectionPlan = protectionPlansIterator.next();
					csvWriter.writeNext(new String[] { ((Long) product.get("sku")).toString(), ((Long) protectionPlan.get("sku")).toString(), score + "" });
					score++;
				}
				finally{
					IOUtils.closeQuietly(csvWriter);
				}
			}
		}
	}

	private void writeToProductFile(JSONObject product, CSVWriter productsCSVWriter) throws Exception {
		JSONArray categoriesArray = (JSONArray) product.get("categoryPath");
		String categoryId = (String) ((JSONObject) (categoriesArray.get(categoriesArray.size() - 1))).get("id");
		String sku = ((Long) product.get("sku")).toString();
		productsCSVWriter.writeNext(new String[] { sku, categoryId, ((String) product.get("name")).replace('\n', ' ') + " (" + sku + ")", (String) product.get("url"), (String) product.get("thumbnailImage"), "NULL", "NULL" });

	}

	private void writeToAttributeFile(JSONObject product, CSVWriter attributesCSVWriter) throws Exception {
		String sku = ((Long) product.get("sku")).toString();
		String attributeName = "Unknown Detail";
		String attributeValue = "";
		String attributeUnit = "NULL";
		// details
		Iterator detailsIterator = ((JSONArray) product.get("details")).iterator();
		while (detailsIterator.hasNext()) {
			JSONObject detail = (JSONObject) detailsIterator.next();
			attributeValue = ((String) (detail.get("value"))).replaceAll("\n", "  ");
			attributeUnit = attributeValue.toLowerCase().endsWith("price") ? "USD" : "NULL";
			attributeName = (String) detail.get("name");
			if (null == attributeName) {
				attributeName = "Unknown Detail";
			} else if (attributeName.equals("Compatibility")) {
				attributesCSVWriter.writeNext(new String[] { sku, "DND_Model_Compatibility", attributeValue, attributeUnit });
			}
			attributesCSVWriter.writeNext(new String[] { sku, attributeName, attributeValue, attributeUnit });
		}

		Iterator featuresIterator = ((JSONArray) product.get("features")).iterator();
		while (featuresIterator.hasNext()) {
			JSONObject feature = (JSONObject) featuresIterator.next();
			if (feature.containsKey("feature")) {
				attributeValue = ((String) (feature.get("feature"))).replaceAll("\n", " : ");
				attributesCSVWriter.writeNext(new String[] { sku, "Product Feature", attributeValue, "NULL" });
			}
		}
		Iterator includedItemListIterator = ((JSONArray) product.get("includedItemList")).iterator();
		while (includedItemListIterator.hasNext()) {
			JSONObject includedItem = (JSONObject) includedItemListIterator.next();
			attributeValue = ((String) (includedItem.get("includedItem"))).replaceAll("\n", " : ").replaceAll("\t", " : ");
			attributesCSVWriter.writeNext(new String[] { sku, "includedItem", attributeValue, "NULL" });
		}

		JSONArray categoriesArray = (JSONArray) product.get("categoryPath");
		String id = (String) ((JSONObject) categoriesArray.get(categoriesArray.size() - 1)).get("id");
		String name = (String) ((JSONObject) categoriesArray.get(categoriesArray.size() - 1)).get("name");
		attributesCSVWriter.writeNext(new String[] { sku, "NativeCategoryId", id, "NULL" });
		attributesCSVWriter.writeNext(new String[] { sku, "IcsCategory", name, "NULL" });

		Object startDate = product.get("startDate");
		if (null != startDate && ((String) startDate) != "") {
			attributeValue = ((String) (product.get("startDate"))).replaceAll("-", " ");
			attributesCSVWriter.writeNext(new String[] { sku, "SkuStartDate", attributeValue, "NULL" });
		}

		if (product.containsKey("orderable")) {
			attributeValue = (String) product.get("orderable");
			attributeValue = null != attributeValue ? attributeValue : "";
		} else {
			attributeValue = "Attribute Not Present";
		}
		attributesCSVWriter.writeNext(new String[] { sku, "Orderable", attributeValue, "NULL" });

		if (product.containsKey("regularPrice") && product.containsKey("salePrice")) {
			Double dollarSaving = (Double) product.get("regularPrice") - (Double) product.get("salePrice");
			attributesCSVWriter.writeNext(new String[] { sku, "DollarSavings", decimalFormat.format(dollarSaving), "USD" });
		}

		if (product.containsKey("outletCenter") && null != product.get("outletCenter")) {
			attributesCSVWriter.writeNext(new String[] { sku, "DND_Outlet_Center", ((Boolean) product.get("outletCenter")).toString().toUpperCase(), "NULL" });
		}

		if (product.containsKey("collection") && null != product.get("collection")) {
			attributesCSVWriter.writeNext(new String[] { sku, "DND_Collection", ((String) product.get("collection")).replace('\t', ' '), "NULL" });
		}

		if (product.containsKey("instantContent") && null != product.get("instantContent")) {
			Iterator<JSONObject> contentArrayIterator = ((JSONArray) product.get("instantContent")).iterator();
			String content = "";
			while (contentArrayIterator.hasNext()) {
				JSONObject next = contentArrayIterator.next();
				if (next.containsKey("provider")) {
					content += (String) next.get("provider") + "|";
				}
			}

			attributesCSVWriter.writeNext(new String[] { sku, "DND_Instant_Content", content.replace('\t', ' '), "NULL" });
			attributesCSVWriter.writeNext(new String[] { sku, "instantContent", content.replace('\t', ' '), "NULL" });
		}

		Properties prop = new Properties();

		prop.load(getClass().getResourceAsStream("/attributeMapping.properties"));
		for (Entry<Object, Object> property : prop.entrySet()) {
			attributeName = (String) property.getKey();
			String attributeReqName = (String) property.getValue();
			if (product.containsKey(attributeName) && null != product.get(attributeName)) {
				attributeUnit = attributeName.toLowerCase().endsWith("price") ? "USD" : "NULL";
				attributesCSVWriter.writeNext(new String[] { sku, attributeReqName, (product.get(attributeName).toString()).replace('\t', ' '), attributeUnit });
			}

		}

	}

	private Map<String, String[]> getProductCategories(JSONObject product) {
		JSONArray categoriesArray = (JSONArray) product.get("categoryPath");
		int size = categoriesArray.size();
		String parentId = null, categoryId = null, categoryName = null;
		Map<String, String[]> categoryProductMap = new HashMap<String, String[]>();
		for (int i = 1; i < size; i++) {
			if (i > 1) {
				parentId = (String) ((JSONObject) categoriesArray.get(i - 1)).get("id");
			} else {
				parentId = "NULL";
			}

			categoryId = (String) ((JSONObject) categoriesArray.get(i)).get("id");
			categoryName = (String) ((JSONObject) categoriesArray.get(i)).get("name");
			categoryProductMap.put(categoryId, new String[] { categoryId, categoryName, parentId });
		}

		return categoryProductMap;

	}

}
