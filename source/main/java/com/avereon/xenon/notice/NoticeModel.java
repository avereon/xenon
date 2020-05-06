package com.avereon.xenon.notice;

import com.avereon.data.Node;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple storage model for notices.
 */
public class NoticeModel extends Node {

	/**
	 * Get the notices. The list is returned in reverse chronological order of the
	 * notice create timestamp.
	 *
	 * @return The notice list
	 */
	List<Notice> getNotices() {
		return getValues( Notice.class ).stream().sorted( new ReverseTimestampComparator() ).collect( Collectors.toList() );
	}

	void addNotice( Notice notice ) {
		setValue( notice.getId(), notice );
	}

	void removeNotice( Notice notice ) {
		setValue( notice.getId(), null );
	}

	void removeAll() {
		getValueKeys().forEach( ( key ) -> setValue( key, null ) );
	}

	private static class ReverseTimestampComparator implements Comparator<Notice> {

		@Override
		public int compare( Notice notice1, Notice notice2 ) {
			return (int)(notice2.getTimestamp() - notice1.getTimestamp());
		}

	}

}
