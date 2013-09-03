package com.imaginea.feedgenerator.util;

import java.io.File;

public class FeedUtils {

    private final String feedHome, project;
    private final int pageSize;

    public FeedUtils(String feedHome, String project, int pageSize) {
        this.feedHome = feedHome;
        this.project = project;
        this.pageSize = pageSize;
    }

    public String getLastUpdateSKUInfoFile() {
        return feedHome + File.separator + project + "/staging/config/last_update_per_sku.txt";
    }

    public String getLastDataFeedRunInfoFile() {
        return feedHome + File.separator + project + "/staging/config/date_time_of_last_datafeed_run.txt";
    }

    public String getDownloadDir() {
        return feedHome + File.separator + project + "/staging/download";
    }

    public String getOutputDir() {
        return feedHome + File.separator + project + "/import_feeds/";
    }

    public int getPageSize() {
        return pageSize;
    }
}
