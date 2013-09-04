package com.imaginea.feedgenerator.process;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.API_KEY;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DATETIME_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.ITEM_TYPES;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.JSON_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.PRODUCT_ATTR_FILTERS;
import static com.imaginea.feedgenerator.util.RestApiUtils.convertDateToStringFormat;
import static com.imaginea.feedgenerator.util.RestApiUtils.convertStringToDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.imaginea.feedgenerator.FeedStatus;
import com.imaginea.feedgenerator.util.FeedUtils;
import com.imaginea.feedgenerator.util.RestApiUtils;

public class UpdateSKUsProcessor extends AbstractFeedProcessor {
    private final String ATTRIBUTES = "show=sku,itemUpdateDate";
    private static final Logger log = Logger.getLogger(UpdateSKUsProcessor.class);

    public UpdateSKUsProcessor(FeedUtils feedUtils) {
        super(feedUtils);
    }

    @Override
    public void process(FeedStatus feedStatus) throws Exception {

        Date lastRunTimeForDataFeed = feedStatus.getLastRunTime();
        log.debug("Retrieving the skus that have been updated since: " + lastRunTimeForDataFeed);
        int currentPageNum = 1;
        List<Long> skusToUpdate = new ArrayList<Long>();
        String priceUpdateDate = convertDateToStringFormat(lastRunTimeForDataFeed, DATETIME_FORMAT);
        String itemDeltaURL = "http://api.remix.bestbuy.com/v1/products(itemUpdateDate>" + priceUpdateDate
                + "&type%20in(" + ITEM_TYPES + ")&" + PRODUCT_ATTR_FILTERS + ")?" + JSON_FORMAT + "&pageSize="
                + getFeedUtils().getPageSize() + '&' + ATTRIBUTES + '&' + API_KEY + "&page=";
        JSONObject jsonObject = getJSONResponse(itemDeltaURL + currentPageNum);

        Long totalProducts = (Long) jsonObject.get("total");
        if (totalProducts == 0) {
            log.warn("There were no products to update.  Exiting now.");
        } else {
            Long totalPages = (Long) jsonObject.get("totalPages");
            log.info(String
                    .format("Checking %s total products that have been updated since %s.  Processing %s total pages with a page size of %s",
                            totalProducts, priceUpdateDate, totalPages, getFeedUtils().getPageSize()));

            for (int i = 0; i < totalPages; i++) {
                if (i != 0) {
                    jsonObject = getJSONResponse(itemDeltaURL + (i + 1));
                }
                Iterator<JSONObject> productsIterator = ((JSONArray) jsonObject.get("products")).iterator();
                while (productsIterator.hasNext()) {
                    JSONObject product = productsIterator.next();
                    Long sku = (Long) product.get("sku");
                    if (product.get("itemUpdateDate").equals(
                            convertDateToStringFormat(feedStatus.getLatestUpdatedTime(sku), DATETIME_FORMAT))) {
                        continue;
                    }
                    feedStatus.setLatestUpdatedTime(sku,
                            convertStringToDateFormat((String) product.get("itemUpdateDate"), DATETIME_FORMAT));
                    skusToUpdate.add(sku);
                }
            }

            feedStatus.setSkusToUpdate(skusToUpdate);
            log.info(String.format("%s of %s products have changed since we last updated them", skusToUpdate.size(),
                    totalProducts));
        }

    }

    public JSONObject getJSONResponse(String url) throws ParseException {
        return RestApiUtils.getJSONResponse(url);
    }

}
