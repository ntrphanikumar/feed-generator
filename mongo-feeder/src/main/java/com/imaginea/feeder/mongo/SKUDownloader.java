package com.imaginea.feeder.mongo;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.API_KEY;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.ITEM_TYPES;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.JSON_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.PRODUCT_ATTR_FILTERS;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.imaginea.feedgenerator.FeedStatus;
import com.imaginea.feedgenerator.process.UpdateSKUsProcessor;
import com.imaginea.feedgenerator.util.FeedUtils;
import com.imaginea.feedgenerator.util.RestApiUtils;

public class SKUDownloader {
    private static final Logger log = Logger.getLogger(SKUDownloader.class);

    private final UpdateSKUsProcessor updateSKUsProcessor;
    private final FeedUtils feedUtils;

    public SKUDownloader(UpdateSKUsProcessor updateSKUsProcessor, FeedUtils feedUtils) {
        this.updateSKUsProcessor = updateSKUsProcessor;
        this.feedUtils = feedUtils;
    }

    public void process(FeedStatus feedStatus) throws Exception {
        updateSKUsProcessor.process(feedStatus);;
        log.info("Retrieving updated SKUs from BBYOpen and writing to files");
        List<Long> updatedSKUs = feedStatus.getSkusToUpdate();
        int i = 0;
        while (i < updatedSKUs.size() && i + 50 < updatedSKUs.size()) {
            List<Long> skuList = updatedSKUs.subList(i, i + 50);
            getProductsInBatch(skuList);
            i = i + skuList.size();
            log.info(String.format("%s of %s Processed", i, updatedSKUs.size()));
        }
        if (i < updatedSKUs.size()) {
            getProductsInBatch(updatedSKUs.subList(i, updatedSKUs.size()));
            log.info(String.format("%s of %s Processed", updatedSKUs.size(), updatedSKUs.size()));
        }
    }

    private void getProductsInBatch(List<Long> skuList) throws ParseException, IOException {
        String url = "http://api.remix.bestbuy.com/v1/products(sku%20in("
                + skuList.toString().substring(1, skuList.toString().length() - 1).replace(", ", ",") + ")&type%20in("
                + ITEM_TYPES + ")&" + PRODUCT_ATTR_FILTERS + ")?show=all&" + JSON_FORMAT + "&pageSize="
                + feedUtils.getPageSize() + '&' + API_KEY;
        JSONObject bbyOpenResp = getJSONResponse(url);
        Iterator<JSONObject> productsIterator = ((JSONArray) bbyOpenResp.get("products")).iterator();
        while (productsIterator.hasNext()) {
            JSONObject product = productsIterator.next();
            System.out.println(product);
        }
    }

    public JSONObject getJSONResponse(String url) throws ParseException {
        return RestApiUtils.getJSONResponse(url);
    }

}
