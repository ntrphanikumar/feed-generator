package com.imaginea.feedgenerator.process;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DATETIME_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.LAST_BBYOPEN_UPDATES_FILE;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.OVER_LAP_TIME_FOR_DATA_FEED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.imaginea.feedgenerator.FeedStatus;

public class PreviousFeedGenerationDataProcessor implements Processor {

    private static final Logger log = Logger.getLogger(PreviousFeedGenerationDataProcessor.class);
    private static final String BBYOPEN_LAST_RUN_TIMESTAMP_FILE = "/staging/bestbuy/config/date_time_of_last_bbyopen_datafeed_run.txt";

    private String dataFeedGeneratorHomeDir;

    @Override
    public void process(FeedStatus feedStatus) throws Exception {
        dataFeedGeneratorHomeDir = feedStatus.getFeedGeneratorHome();
        Map<Long, Date> skuLatestUpdatedTimeStamps = new HashMap<Long, Date>();
        File dataFeedLastRunTimeStampFile = new File(dataFeedGeneratorHomeDir + BBYOPEN_LAST_RUN_TIMESTAMP_FILE);
        if (dataFeedLastRunTimeStampFile.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFeedLastRunTimeStampFile));
            try {
                Date lastRunTimeOfDataFeed = new SimpleDateFormat(DATETIME_FORMAT).parse(bufferedReader.readLine());
                log.debug("Date of last BBYOpen Datafeed Generator run: " + lastRunTimeOfDataFeed);
                lastRunTimeOfDataFeed.setTime(lastRunTimeOfDataFeed.getTime() - OVER_LAP_TIME_FOR_DATA_FEED);
                log.info("Over Lapping Data Feed to hours : " + lastRunTimeOfDataFeed);
                skuLatestUpdatedTimeStamps = getSKUsUpdatedTimeStampsFromLastRun();
                feedStatus.setSkuLatestUpdatedTimeStamps(skuLatestUpdatedTimeStamps);
                feedStatus.setLastRunTimeForDataFeed(lastRunTimeOfDataFeed);
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
    }

    private Map<Long, Date> getSKUsUpdatedTimeStampsFromLastRun() throws Exception {
        Map<Long, Date> skuLatestUpdatedTimeStamps = new HashMap<Long, Date>();
        File lastSKUUpdatedTimeStamps = new File(dataFeedGeneratorHomeDir + LAST_BBYOPEN_UPDATES_FILE);
        if (!lastSKUUpdatedTimeStamps.exists()) {
            log.warn("No " + dataFeedGeneratorHomeDir + LAST_BBYOPEN_UPDATES_FILE + " file found. ");
        } else {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(lastSKUUpdatedTimeStamps));
            try {
                String skuTimeStampRecord;
                while ((skuTimeStampRecord = bufferedReader.readLine()) != null) {
                    String[] skuTimeStamps = skuTimeStampRecord.split("\t");
                    skuLatestUpdatedTimeStamps.put(new Long(skuTimeStamps[0]),
                            new SimpleDateFormat(DATETIME_FORMAT).parse(skuTimeStamps[1]));
                }
            } catch (IOException exception) {
                log.debug("Critical exception: " + exception.getCause());
                throw exception;
            } catch (java.text.ParseException exception) {
                log.debug("Critical exception: " + exception.getCause());
                throw exception;
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
        return skuLatestUpdatedTimeStamps;

    }

}
