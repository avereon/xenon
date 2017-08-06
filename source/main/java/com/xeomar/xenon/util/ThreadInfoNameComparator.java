package com.xeomar.xenon.util;

import java.lang.management.ThreadInfo;
import java.util.Comparator;

public class ThreadInfoNameComparator implements Comparator<ThreadInfo> {

	@Override
	public int compare( ThreadInfo threadInfo1, ThreadInfo threadInfo2 ) {
		return threadInfo1.getThreadName().compareToIgnoreCase( threadInfo2.getThreadName() );
	}

}
