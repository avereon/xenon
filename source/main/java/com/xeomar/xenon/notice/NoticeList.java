package com.xeomar.xenon.notice;

import com.xeomar.xenon.node.Node;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NoticeList extends Node {

	public List<Notice> getNotices() {
		return getValues( Notice.class ).stream().sorted( new ReverseTimestampComparator() ).collect( Collectors.toList() );
	}

	public void addNotice( Notice notice ) {
		setValue( notice.getId(), notice );
	}

	public void removeNotice( Notice notice ) {
		setValue( notice.getId(), null );
	}

	private class ReverseTimestampComparator implements Comparator<Notice> {

		@Override
		public int compare( Notice notice1, Notice notice2 ) {
			return (int)(notice2.getTimestamp() - notice1.getTimestamp());
		}

	}

}
