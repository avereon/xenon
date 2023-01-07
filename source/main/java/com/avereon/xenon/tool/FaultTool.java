package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.xenon.ProgramEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import javafx.scene.control.TextArea;
import lombok.CustomLog;

import java.io.PrintWriter;
import java.io.StringWriter;

@CustomLog
public class FaultTool extends ProgramTool {

	private final TextArea text;

	private EventHandler<ProgramEvent> closingHandler;

	public FaultTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-fault" );

		text = new TextArea();
		text.setId( "tool-fault-text" );
		text.setEditable( false );

		getChildren().addAll( text );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getAsset().getName() );
		setGraphic( getProgram().getIconLibrary().getIcon( "fault" ) );
		// Tasks have to finish before the program exits so this ensures the tool will close
		getProgram().register( ProgramEvent.STOPPING, closingHandler = ( e ) -> getProgram().getTaskManager().submit( Task.of( "", this::close ) ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		Throwable throwable = getAsset().getModel();

		if( throwable != null ) {
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter( writer );
			throwable.printStackTrace( printer );
			text.setText( writer.toString() );
		}
	}

	@Override
	protected void deallocate() {
		getProgram().unregister( ProgramEvent.STOPPING, closingHandler );
	}

}
