package com.imaginea.feedgenerator.process;

import static com.imaginea.feedgenerator.FeedGeneratorConstansts.DATETIME_FORMAT;
import static com.imaginea.feedgenerator.FeedGeneratorConstansts.LAST_BBYOPEN_UPDATES_FILE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import com.imaginea.feedgenerator.FeedGenerator;
import com.imaginea.feedgenerator.FeedStatus;

public class UpdateCurrentFeedGenerationDataProcessor implements Processor {

    private static final Logger log = Logger.getLogger(FeedGenerator.class);
    private static final String BBYOPEN_LAST_RUN_TIMESTAMP_FILE = "/staging/bestbuy/config/date_time_of_last_bbyopen_datafeed_run.txt";

    public void process(FeedStatus feedStatus) throws Exception {
        updateLastRunDate(feedStatus);
        updateSKUUpdateTimes(feedStatus);
    }

    private void updateSKUUpdateTimes(FeedStatus feedStatus) throws IOException, ParseException {
        File lastSKUUpdatedTimeStamps = new File(feedStatus.getFeedGeneratorHome() + LAST_BBYOPEN_UPDATES_FILE);
        if (!lastSKUUpdatedTimeStamps.exists()) {
            lastSKUUpdatedTimeStamps.createNewFile();
        }
        CSVWriter csvWriter = new CSVWriter(new FileWriter(lastSKUUpdatedTimeStamps), '\t',
                CSVWriter.NO_QUOTE_CHARACTER);
        try {
            for (Entry<Long, Date> record : feedStatus.getSkuLatestUpdatedTimeStamps().entrySet()) {
                csvWriter.writeNext(new String[] { record.getKey().toString(),
                        new SimpleDateFormat(DATETIME_FORMAT).format(record.getValue()) });
            }
        } finally {
            IOUtils.closeQuietly(csvWriter);
        }

    }

    private void updateLastRunDate(FeedStatus feedStatus) throws IOException {
        log.debug("Recording a last up date time of " + feedStatus.getStartTime());
        File dataFeedLastRunTimeStampFile = new File(feedStatus.getFeedGeneratorHome()
                + BBYOPEN_LAST_RUN_TIMESTAMP_FILE);
        FileWriter fw = new FileWriter(dataFeedLastRunTimeStampFile);
        try {
            fw.write(new SimpleDateFormat(DATETIME_FORMAT).format(feedStatus.getStartTime()));
        } finally {
            IOUtils.closeQuietly(fw);
        }

    }
}
