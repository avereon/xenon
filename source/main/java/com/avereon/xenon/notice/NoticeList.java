package com.avereon.xenon.notice;

import com.avereon.data.Node;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NoticeList extends Node {

	List<Notice> getNotices() {
		return getValues( Notice.class ).stream().sorted( new ReverseTimestampComparator() ).collect( Collectors.toList() );
	}

	//	public Notice getNotice( String id ) {
	//		return getValue( id );
	//	}

	void addNotice( Notice notice ) {
		setValue( notice.getId(), notice );
	}

	void removeNotice( Notice notice ) {
		setValue( notice.getId(), null );
	}

	void clearAll() {
		getValueKeys().forEach( ( key ) -> setValue( key, null ) );
	}

	//	public int size() {
	//		return getValues( Notice.class ).size();
	//	}

	private static class ReverseTimestampComparator implements Comparator<Notice> {

		@Override
		public int compare( Notice notice1, Notice notice2 ) {
			return (int)(notice2.getTimestamp() - notice1.getTimestamp());
		}

	}

}
