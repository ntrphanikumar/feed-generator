package com.imaginea.feedgenerator.process;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DATETIME_FORMAT;
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
import com.imaginea.feedgenerator.util.FeedUtils;

public class PreviousFeedGenerationDataProcessor extends AbstractFeedProcessor {

    private static final Logger log = Logger.getLogger(PreviousFeedGenerationDataProcessor.class);

    public PreviousFeedGenerationDataProcessor(FeedUtils feedUtils) {
        super(feedUtils);
    }

    @Override
    public void process(FeedStatus feedStatus) throws Exception {
        Map<Long, Date> skuLatestUpdatedTimeStamps = new HashMap<Long, Date>();
        File dataFeedLastRunTimeStampFile = new File(getFeedUtils().getLastDataFeedRunInfoFile());
        if (dataFeedLastRunTimeStampFile.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFeedLastRunTimeStampFile));
            try {
                Date lastRunTimeOfDataFeed = new SimpleDateFormat(DATETIME_FORMAT).parse(bufferedReader.readLine());
                log.debug("Date of last BBYOpen Datafeed Generator run: " + lastRunTimeOfDataFeed);
                lastRunTimeOfDataFeed.setTime(lastRunTimeOfDataFeed.getTime() - OVER_LAP_TIME_FOR_DATA_FEED);
                log.info("Over Lapping Data Feed to hours : " + lastRunTimeOfDataFeed);
                skuLatestUpdatedTimeStamps = getSKUsUpdatedTimeStampsFromLastRun();
                feedStatus.setSkuLatestUpdatedTimeStamps(skuLatestUpdatedTimeStamps);
                feedStatus.setLastRunTime(lastRunTimeOfDataFeed);
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
    }

    private Map<Long, Date> getSKUsUpdatedTimeStampsFromLastRun() throws Exception {
        Map<Long, Date> skuLatestUpdatedTimeStamps = new HashMap<Long, Date>();
        File lastSKUUpdatedTimeStamps = new File(getFeedUtils().getLastUpdateSKUInfoFile());
        if (!lastSKUUpdatedTimeStamps.exists()) {
            log.warn("No " + getFeedUtils().getLastUpdateSKUInfoFile() + " file found. ");
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
