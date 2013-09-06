package com.imaginea.feeder.mongo;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.API_KEY;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.JSON_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.PRODUCT_ATTR_FILTERS;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.imaginea.feedgenerator.FeedStatus;
import com.imaginea.feedgenerator.util.FeedUtils;
import com.imaginea.feedgenerator.util.RestApiUtils;

public class SKUDownloader {
    private static final Logger log = Logger.getLogger(SKUDownloader.class);

    class ProductMetaInfoRunnable implements Callable<ProductsMetaInfo> {

        private final int pageNumber;

        public ProductMetaInfoRunnable(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public ProductsMetaInfo call() {
            try {
                String productsMetaInfo = RestApiUtils.executeGet(getUpdatedProductsInfoURL(pageNumber,
                        feedUtils.getPageSize()));
                ProductsMetaInfo metaInfo = mapper.readValue(productsMetaInfo, ProductsMetaInfo.class);
                for (ObjectNode product : metaInfo.getProducts()) {
                    productDAO.addProduct(product.toString());
                }
                return metaInfo;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getUpdatedProductsInfoURL(int pageNumber, int pageSize) throws java.text.ParseException {
            return "http://api.remix.bestbuy.com/v1/products(" + PRODUCT_ATTR_FILTERS + ")?" + JSON_FORMAT
                    + "&show=all&" + API_KEY + "&pageSize=" + pageSize + "&page=" + pageNumber;
        }

    }

    private final ProductDAO productDAO;
    private final FeedUtils feedUtils;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = new ThreadPoolExecutor(20, 20, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public SKUDownloader(ProductDAO productDAO, FeedUtils feedUtils) {
        this.productDAO = productDAO;
        this.feedUtils = feedUtils;
    }

    public void process(FeedStatus feedStatus) throws Exception {
        String url = "http://api.remix.bestbuy.com/v1/products(" + PRODUCT_ATTR_FILTERS + ")?" + JSON_FORMAT
                + "&show=sku&" + API_KEY + "&pageSize=1&page=1";
        ProductsMetaInfo metaInfo = mapper.readValue(RestApiUtils.executeGet(url), ProductsMetaInfo.class);
        for (int pageNumber = 1; pageNumber <= metaInfo.getTotalPages(); pageNumber++) {
            executor.submit(new ProductMetaInfoRunnable(pageNumber));
        }
    }
}
