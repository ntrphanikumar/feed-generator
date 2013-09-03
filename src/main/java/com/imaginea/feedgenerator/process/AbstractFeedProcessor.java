package com.imaginea.feedgenerator.process;

import com.imaginea.feedgenerator.util.FeedUtils;

abstract class AbstractFeedProcessor implements FeedProcessor {

    private final FeedUtils feedUtils;

    public AbstractFeedProcessor(FeedUtils feedUtils) {
        this.feedUtils = feedUtils;
    }

    FeedUtils getFeedUtils() {
        return feedUtils;
    }
}
