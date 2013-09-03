package com.imaginea.feedgenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedStatus {

    private final String feedGeneratorHome;
    private final Date startTime;

    private Date lastRunTimeForDataFeed;
    private Map<Long, Date> skuLatestUpdatedTimeStamps;
    private List<Long> skusToUpdate;

    public FeedStatus(String feedGeneratorHome, Date startTime) {
        this.feedGeneratorHome = feedGeneratorHome;
        this.startTime = startTime;
        lastRunTimeForDataFeed = new Date(0);
    }

    public String getFeedGeneratorHome() {
        return feedGeneratorHome;
    }

    public List<Long> getSkusToUpdate() {
        if (skusToUpdate == null) {
            skusToUpdate = new ArrayList<Long>();
        }
        return skusToUpdate;
    }

    public void setSkusToUpdate(List<Long> skusToUpdate) {
        this.skusToUpdate = skusToUpdate;
    }

    public Date getLastRunTimeForDataFeed() {
        return lastRunTimeForDataFeed;
    }

    public void setLastRunTimeForDataFeed(Date lastRunTimeForDataFeed) {
        this.lastRunTimeForDataFeed = lastRunTimeForDataFeed;
    }

    public Date getLatestUpdatedTime(Long sku) {
        Date latestUpdatedTime = getSkuLatestUpdatedTimeStamps().get(sku);
        if (latestUpdatedTime == null) {
            latestUpdatedTime = lastRunTimeForDataFeed;
        }
        return latestUpdatedTime;
    }

    public void setSkuLatestUpdatedTimeStamps(Map<Long, Date> skuLatestUpdatedTimeStamps) {
        this.skuLatestUpdatedTimeStamps = skuLatestUpdatedTimeStamps;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setLatestUpdatedTime(Long sku, Date latestUpdatedTime) {
        getSkuLatestUpdatedTimeStamps().put(sku, latestUpdatedTime);
    }

    public Map<Long, Date> getSkuLatestUpdatedTimeStamps() {
        if (skuLatestUpdatedTimeStamps == null) {
            skuLatestUpdatedTimeStamps = new HashMap<Long, Date>();
        }
        return skuLatestUpdatedTimeStamps;
    }
}
