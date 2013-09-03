package com.imaginea.feedgenerator.process;

import com.imaginea.feedgenerator.FeedStatus;

public interface FeedProcessor {
	void process(FeedStatus feedStatus) throws Exception;
}
