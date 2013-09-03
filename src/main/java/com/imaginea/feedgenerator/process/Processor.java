package com.imaginea.feedgenerator.process;

import com.imaginea.feedgenerator.FeedStatus;

public interface Processor {
	void process(FeedStatus feedStatus) throws Exception;
}
