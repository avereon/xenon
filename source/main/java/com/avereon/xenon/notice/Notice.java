package com.avereon.xenon.notice;

import com.avereon.data.IdNode;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import com.avereon.util.HashUtil;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.CustomLog;

import java.text.MessageFormat;

@CustomLog
public class Notice extends IdNode {

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

		public String getUnreadIcon() {
			return "notice-unread-" + name().toLowerCase();
		}

	}

	private static final String TIMESTAMP = "timestamp";

	private static final String TYPE = "type";

	private static final String BALLOON_STICKINESS = "balloon";

	private static final String TITLE = "title";

	private static final String MESSAGE = "message";

	private static final String CAUSE = "throwable";

	private static final String ACTION = "action";

	private static final String READ = "read";

	private final Object[] parameters;

	public Notice(  ) {
		this( null, null, null, null, new Object[0] );
	}

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
		defineNaturalKey( ID );

		this.parameters = parameters;

		try( Txn ignored = Txn.create( true ) ) {
			setId( HashUtil.hash( title + getMessageStringContent( message ) ) );

			setValue( TIMESTAMP, System.currentTimeMillis() );
			setValue( TITLE, title );
			setValue( MESSAGE, message );
			setValue( CAUSE, throwable );
			setValue( BALLOON_STICKINESS, Balloon.NORMAL );

			setType( Type.NORM );
			setAction( action );

			setModified( false );
		} catch( TxnException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	public Long getTimestamp() {
		return getValue( TIMESTAMP );
	}

	public Notice setTimestamp( Long timestamp ) {
		setValue( TIMESTAMP, timestamp );
		return this;
	}

	public String getTitle() {
		return getValue( TITLE );
	}

	public Notice setTitle( String title ) {
		setValue( TITLE, title );
		return this;
	}

	public Object getMessage() {
		return getValue( MESSAGE );
	}

	public Notice setMessage( Object message ) {
		setValue( MESSAGE, message );
		return this;
	}

	public Throwable getCause() {
		return getValue( CAUSE );
	}

	public Notice setCause( Throwable throwable ) {
		setValue( CAUSE, throwable );
		return this;
	}

	public Runnable getAction() {
		return getValue( ACTION );
	}

	public Notice setAction( Runnable action ) {
		setValue( ACTION, action );
		return this;
	}

	public Type getType() {
		return getValue( TYPE );
	}

	public Notice setType( Type type ) {
		setValue( TYPE, type );
		return this;
	}

	public boolean isRead() {
		return getValue( READ, false );
	}

	public Notice setRead( boolean read ) {
		setValue( READ, read );
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
		return formatMessage( getMessage(), getCause() );
	}

	private String formatMessage( Object message, Throwable throwable ) {
		String string = message == null ? null : getMessageStringContent( message );
		if( string == null && throwable != null ) return throwable.getLocalizedMessage();
		return string;
	}

	private String getMessageStringContent( Object message ) {
		StringBuilder builder = new StringBuilder();

		if( message instanceof javafx.scene.Node ) {
			if( message instanceof TextInputControl ) {
				// Handle text input controls
				builder = new StringBuilder( ((TextInputControl)message).getText() );
			} else if( message instanceof TextFlow flow ) {
				// Handle text flow nodes
				for( javafx.scene.Node node : flow.getChildren() ) {
					builder.append( ((Text)node).getText() );
				}
			} else {
				builder = new StringBuilder( message.toString() );
			}
		} else {
			builder = new StringBuilder( message == null ? "null" : message.toString().trim() );
		}

		return MessageFormat.format( builder.toString(), parameters );
	}

}
