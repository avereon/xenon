package com.avereon.xenon.notice;

import com.avereon.util.HashUtil;
import com.avereon.util.LogUtil;
import com.avereon.xenon.node.Node;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class Notice extends Node {

	public enum NoticeType {
		NONE,
		INFO,
		WARN,
		ERROR
	}

	public static final String BALLOON_ALWAYS = "balloon-always";

	public static final String BALLOON_NORMAL = "balloon-normal";

	public static final String BALLOON_NEVER = "balloon-never";

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String ID = "id";

	private static final String TIMESTAMP = "timestamp";

	// TODO How severe/important is this message ???
	// Is this just a range 1-N? Or are these named?
	private static final String SEVERITY = "severity";

	// TODO What type of message is this message ???
	// Similar to the Alert.AlertType: NONE, INFORMATION, WARNING, CONFIRMATION, ERROR
	private static final String TYPE = "type";

	private static final String BALLOON_STICKINESS = "balloon";

	private static final String TITLE = "title";

	private static final String MESSAGE = "message";

	private static final String ACTION = "action";

	private static final String READ = "read";

	public Notice( String title, String message ) {
		this( title, message, null );
	}

	public Notice( String title, String message, boolean read ) {
		this( title, message, read, null );
	}

	public Notice( String title, String message, Runnable action ) {
		this( title, message, false, action );
	}

	/**
	 * Create a notice.
	 *
	 * @param title The title for the notice show in bold
	 * @param message The message for the notice
	 * @param read Should the notice be marked as already read
	 * @param action A runnable action that will be executed when the user clicks on the notice
	 */
	public Notice( String title, String message, boolean read, Runnable action ) {
		definePrimaryKey( ID );
		defineNaturalKey( TITLE );

		setValue( ID, HashUtil.hash( title + message ) );
		setValue( TIMESTAMP, System.currentTimeMillis() );
		setValue( TITLE, title );
		setValue( MESSAGE, message );
		setValue( ACTION, action );
		setValue( BALLOON_STICKINESS, BALLOON_NORMAL );
		setFlag( READ, read );
		setModified( false );
	}

	public String getId() {
		return getValue( ID );
	}

	public Long getTimestamp() {
		return getValue( TIMESTAMP );
	}

	public String getTitle() {
		return getValue( TITLE );
	}

	public String getMessage() {
		return getValue( MESSAGE );
	}

	public Runnable getAction() {
		return getValue( ACTION );
	}

	public NoticeType getType() {
		return getValue( TYPE );
	}

	public Notice setType( NoticeType type ) {
		setValue( TYPE, type );
		return this;
	}

	public boolean isRead() {
		return getFlag( READ );
	}

	public Notice setRead( boolean read ) {
		setFlag( READ, read );
		return this;
	}

	public String getBalloonStickiness() {
		return getValue( BALLOON_STICKINESS );
	}

	public Notice setBalloonStickiness( String value ) {
		boolean modified = isModified();
		setValue( BALLOON_STICKINESS, value );
		if( !modified ) setModified( false );
		return this;
	}

}
