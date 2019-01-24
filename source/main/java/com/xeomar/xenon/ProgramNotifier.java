package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.util.DialogUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * @deprecated In favor of {@link com.xeomar.xenon.notice.NoticeManager}
 */
@Deprecated
public class ProgramNotifier {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	public ProgramNotifier( Program program ) {
		this.program = program;
	}

	public void error( Object message ) {
		error( null, message );
	}

	/* Error methods */

	public void error( Throwable throwable ) {
		error( null, null, throwable );
	}

	public void error( String title, Throwable throwable ) {
		error( title, null, throwable );
	}

	public void error( Throwable throwable, Object message, String... parameters ) {
		error( null, throwable, message, parameters );
	}

	public void error( String title, Throwable throwable, Object message, String... parameters ) {
		alert( Alert.AlertType.ERROR, null, title, null, formatMessage( throwable, message ), parameters );
	}

	/* Warning methods */

	public void warning( Object message, String... parameters ) {
		warning( null, message, parameters );
	}

	public void warning( String title, Object message, String... parameters ) {
		alert( Alert.AlertType.WARNING, null, title, null, message, parameters );
	}

	/* Notify methods */

	public void notify( String title, String header, Object message, String... parameters ) {
		notify( null, title, header, message, parameters );
	}

	public void notify( Node icon, String title, String header, Object message, String... parameters ) {
		alert( Alert.AlertType.INFORMATION, icon, title, header, message, parameters );
	}

	private Object formatMessage( Throwable throwable, Object message ) {
		String string = message == null ? null : message.toString();
		log.error( string, throwable );

		if( message == null && throwable != null ) return throwable.getLocalizedMessage();
		return message;
	}

	private void alert( Alert.AlertType type, Node icon, String title, String header, Object message, String... parameters ) {
		try {
			final String content = getAlertContent( message, parameters );

			log.info( content );

			Platform.runLater( () -> {
				Alert alert = new Alert( Alert.AlertType.NONE, content );
				if( message instanceof Node ) alert.getDialogPane().setContent( (Node)message );
				if( type != null ) alert.setAlertType( type );
				if( title != null ) alert.setTitle( title );
				if( header != null ) alert.setHeaderText( header );
				if( icon != null ) alert.setGraphic( icon );
				DialogUtil.show( program.getWorkspaceManager().getActiveWorkspace().getStage(), alert );
			} );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.out );
		}
	}

	private String getAlertContent( Object message, Object[] parameters ) {
		StringBuilder builder = new StringBuilder();

		if( message instanceof Node ) {
			if( message instanceof TextInputControl ) {
				// Handle text input controls
				builder = new StringBuilder( ((TextInputControl)message).getText() );
			} else if( message instanceof TextFlow ) {
				// Handle text flow nodes
				TextFlow flow = (TextFlow)message;
				for( Node node : flow.getChildren() ) {
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

}
