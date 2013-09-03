package com.imaginea.feedgenerator;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.imaginea.feedgenerator.process.Processor;

public class FeedGenerator {

    private static final Logger log = Logger.getLogger(FeedGenerator.class);

    private final FeedStatus feedStatus;
    private final List<Processor> feedExecutionLinks;

    public FeedGenerator(FeedStatus feedStatus, List<Processor> feedExecutionLinks) {
        this.feedStatus = feedStatus;
        this.feedExecutionLinks = feedExecutionLinks;
    }

    public void generate() throws Exception {
        log.info("Starting Data Feed Generator at : " + feedStatus.getStartTime());
        for (Processor executionLink : feedExecutionLinks) {
            executionLink.process(feedStatus);
        }
        log.info(String.format("Time Data Feed Generator had run  : %s secs", (new Date().getTime() - feedStatus
                .getStartTime().getTime()) / 1000));
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext-*.xml");
        context.getBean(FeedGenerator.class).generate();
    }

}
