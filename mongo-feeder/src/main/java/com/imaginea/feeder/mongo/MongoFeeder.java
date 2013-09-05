package com.imaginea.feeder.mongo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.imaginea.feedgenerator.FeedStatus;

public class MongoFeeder {

    private final SKUDownloader skuDownloader;
    private final FeedStatus feedStatus;

    public MongoFeeder(SKUDownloader skuDownloader, FeedStatus feedStatus) {
        this.skuDownloader = skuDownloader;
        this.feedStatus = feedStatus;
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext-*.xml");
        context.getBean(MongoFeeder.class).feed();
    }

    public void feed() {
        try {
            skuDownloader.process(feedStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
