package com.imaginea.feedgenerator.process;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.API_KEY;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DATETIME_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DOWNLOAD_DIR;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.ITEM_TYPES;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.JSON_FORMAT;
import static com.imaginea.feedgenerator.util.Utils.convertDateToStringFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.imaginea.feedgenerator.FeedStatus;
import com.imaginea.feedgenerator.util.Utils;

public class DeleteInactiveSKUsProcessor implements Processor {
    private static final Logger log = Logger.getLogger(DownloadSKUDataFilesProcessor.class);
    private final String ATTRIBUTES = "show=sku";
    private String dataFeedGeneratorHomeDir;

    private final int pageSize;

    public DeleteInactiveSKUsProcessor(int pageSize) {
        this.pageSize = pageSize;
    }

    public JSONObject getJSONResponse(String url) throws ParseException {
        return Utils.getJSONResponse(url);
    }

    public void process(FeedStatus feedStatus) throws Exception {
        dataFeedGeneratorHomeDir = feedStatus.getFeedGeneratorHome();
        Date lastRunTimeForDataFeed = feedStatus.getLastRunTime();
        log.debug(String.format("Deleting the skus that have become inactive since last update time of %s ",
                lastRunTimeForDataFeed));
        List<Long> skusToDelete = new ArrayList<Long>();
        int currentPageNum = 1;
        String lastUpdateDate = convertDateToStringFormat(lastRunTimeForDataFeed, DATETIME_FORMAT);
        String itemDeltaURL = "http://api.remix.bestbuy.com/v1/products(itemUpdateDate>" + lastUpdateDate
                + "&type%20in(" + ITEM_TYPES + ")&active=false)?" + JSON_FORMAT + "&pageSize=" + pageSize + '&'
                + ATTRIBUTES + '&' + API_KEY + "&page=";
        JSONObject jsonObject = getJSONResponse(itemDeltaURL + currentPageNum);
        Long totalProducts = (Long) jsonObject.get("total");
        if (totalProducts == 0) {
            log.info("There were no products to delete.");
        } else {
            Long totalPages = (Long) jsonObject.get("totalPages");
            log.info(String
                    .format("Deleting %s total products that have been updated since %s.  Processing %s total pages with a page size of %s",
                            totalProducts, lastUpdateDate, totalPages, pageSize));

            for (int i = 0; i < totalPages; i++) {
                if (i != 0) {
                    jsonObject = getJSONResponse(itemDeltaURL + (i + 1));
                }
                Iterator<JSONObject> productsIterator = ((JSONArray) jsonObject.get("products")).iterator();
                while (productsIterator.hasNext()) {
                    JSONObject product = productsIterator.next();
                    Long sku = (Long) product.get("sku");
                    File fileToBeRemoved = new File(dataFeedGeneratorHomeDir + DOWNLOAD_DIR + File.separator + sku
                            + ".js");
                    log.debug("File to be removed : " + fileToBeRemoved.getName());
                    log.debug("File to be removed exits : " + fileToBeRemoved.exists());
                    if (fileToBeRemoved.exists()) {
                        if (fileToBeRemoved.delete()) {
                            skusToDelete.add(sku);
                        } else {
                            log.error("Error Deleting File : " + fileToBeRemoved.getName());
                        }
                    }
                }
            }
            log.warn("Removed Files For SKUs " + skusToDelete.toString());
        }
    }

}
