package com.avereon.xenon.notice;

import com.avereon.util.HashUtil;
import com.avereon.util.LogUtil;
import com.avereon.xenon.node.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class Notice extends Node {

	public enum Balloon {

		NEVER,
		NORMAL,
		ALWAYS

	}

	public enum Type {

		NONE,
		NORM,
		INFO,
		WARN,
		ERROR;

		public String getIcon() {
			return "notice-" + name().toLowerCase();
		}

	}

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String ID = "id";

	private static final String TIMESTAMP = "timestamp";

	private static final String TYPE = "type";

	private static final String BALLOON_STICKINESS = "balloon";

	private static final String TITLE = "title";

	private static final String MESSAGE = "message";

	private static final String THROWABLE = "throwable";

	private static final String ACTION = "action";

	private static final String READ = "read";

	private Object[] parameters;

	public Notice( Object title, Object message, Object... parameters ) {
		this( title, message, null, null, parameters );
	}

	public Notice( Object title, Object message, Throwable throwable, Object... parameters ) {
		this( title, message, throwable, null, parameters );
	}

	public Notice( Object title, Object message, Runnable action, Object... parameters ) {
		this( title, message, null, action, parameters );
	}

	/**
	 * Create a notice.
	 *
	 * @param title The title for the notice show in bold
	 * @param message The message for the notice
	 * @param throwable The throwable to use with the notice
	 * @param action A runnable action that will be executed when the user clicks on the notice
	 * @param parameters Parameters to be used in the message
	 */
	public Notice( Object title, Object message, Throwable throwable, Runnable action, Object... parameters ) {
		definePrimaryKey( ID );
		defineNaturalKey( TITLE );

		this.parameters = parameters;

		setValue( TIMESTAMP, System.currentTimeMillis() );
		setValue( TITLE, title );
		setValue( MESSAGE, message );
		setValue( THROWABLE, throwable );
		setValue( ACTION, action );
		setValue( TYPE, Type.NORM );
		setValue( BALLOON_STICKINESS, Balloon.NORMAL );
		setValue( ID, HashUtil.hash( title + getMessageStringContent() ) );
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

	public Object getMessage() {
		return getValue( MESSAGE );
	}

	public Throwable getThrowable() {
		return getValue( THROWABLE );
	}

	public Runnable getAction() {
		return getValue( ACTION );
	}

	public Type getType() {
		return getValue( TYPE );
	}

	public Notice setType( Type type ) {
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

	public Balloon getBalloonStickiness() {
		return getValue( BALLOON_STICKINESS );
	}

	public Notice setBalloonStickiness( Balloon value ) {
		boolean modified = isModified();
		setValue( BALLOON_STICKINESS, value );
		if( !modified ) setModified( false );
		return this;
	}

	String getFormattedMessage() {
		return formatMessage( getMessage(), getThrowable() );
	}

	private String formatMessage( Object message, Throwable throwable ) {
		String string = message == null ? null : getMessageStringContent();

		switch( getType() ) {
			case ERROR: {
				error( string, throwable );
				break;
			}
			case WARN: {
				warn( string, throwable );
				break;
			}
		}

		if( string == null && throwable != null ) return throwable.getLocalizedMessage();
		return string;
	}

	private String getMessageStringContent() {
		StringBuilder builder = new StringBuilder();

		Object message = getMessage();
		if( message instanceof javafx.scene.Node ) {
			if( message instanceof TextInputControl ) {
				// Handle text input controls
				builder = new StringBuilder( ((TextInputControl)message).getText() );
			} else if( message instanceof TextFlow ) {
				// Handle text flow nodes
				TextFlow flow = (TextFlow)message;
				for( javafx.scene.Node node : flow.getChildren() ) {
					Text text = (Text)node;
					builder.append( text.getText() );
				}
			} else {
				builder = new StringBuilder( message.toString() );
			}
		} else {
			builder = new StringBuilder( message == null ? "null" : message.toString().trim() );
		}

		return String.format( builder.toString(), parameters );
	}

	private void error(String message, Throwable throwable ) {
		log.error( message, throwable );
	}

	private void warn(String message, Throwable throwable ) {
		log.warn( message, throwable );
	}

}
