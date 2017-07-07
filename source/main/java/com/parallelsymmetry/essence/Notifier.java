package com.parallelsymmetry.essence;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifier {

	private static final Logger log = LoggerFactory.getLogger( Notifier.class );

	private Program program;

	public Notifier( Program program ) {
		this.program = program;
	}

	public void notify( Object message ) {
		notify( null, message );
	}

	public void notify( Object message, Alert.AlertType type ) {
		notify( null, message, type );
	}

	public void notify( Object message, Alert.AlertType type, Node graphic ) {
		notify( null, message, type, graphic );
	}

	public void notify( String title, Object message ) {
		notify( title, message, Alert.AlertType.INFORMATION );
	}

	public void notify( String title, Object message, Alert.AlertType type ) {
		notify( title, message, type, null );
	}

	public void notify( String title, Object message, Alert.AlertType type, Node graphic ) {
		StringBuilder content = new StringBuilder();

		if( message instanceof Node ) {
			if( message instanceof TextInputControl ) {
				// Handle text input controls
				content = new StringBuilder( ((TextInputControl)message).getText() );
			} else if( message instanceof TextFlow ) {
				// Handle text flow nodes
				TextFlow flow = (TextFlow)message;
				for( Node node : flow.getChildren() ) {
					Text text = (Text)node;
					content.append( text.getText() );
				}
			} else {
				content = new StringBuilder( message.toString() );
			}
		} else {
			content = new StringBuilder( message == null ? "null" : message.toString().trim() );
		}

		log.info( content.toString() );

		Alert alert = new Alert( Alert.AlertType.NONE );
		if( message instanceof Node ) alert.getDialogPane().setContent( (Node)message );
		alert.setContentText( content.toString() );
		if( type != null ) alert.setAlertType( type );
		if( title != null ) alert.setTitle( title );
		if( graphic != null ) alert.setGraphic( graphic );
		Platform.runLater( alert::show );
	}

	public void error( Object message ) {
		error( null, message, null );
	}

	public void error( Throwable throwable ) {
		error( null, null, throwable );
	}

	public void error( Object message, Throwable throwable ) {
		error( null, message, throwable );
	}

	public void error( String title, Object message ) {
		error( title, message, null );
	}

	public void error( String title, Throwable throwable ) {
		error( title, null, throwable );
	}

	public void error( String title, Object message, Throwable throwable ) {
		notify( title, formatMessage( message, throwable ), Alert.AlertType.ERROR );
	}

	private Object formatMessage( Object message, Throwable throwable ) {
		String string = message == null ? null : message.toString();
		log.error( string, throwable );

		if( message == null && throwable != null ) return throwable.getLocalizedMessage();
		return message;
	}

}
