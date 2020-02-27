package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.xenon.ProgramEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FaultTool extends ProgramTool {

	private TextArea text;

	private EventHandler<ProgramEvent> closingHandler;

	public FaultTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-fault" );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "fault" ) );

		text = new TextArea();
		text.setId( "tool-fault-text" );
		text.setEditable( false );

		getChildren().addAll( text );

		getProgram().register( ProgramEvent.STOPPING, closingHandler = ( e ) -> {
			// Tasks have to finish before the program exists so this ensures the tool will close
			getProgram().getTaskManager().submit( Task.of( "", this::close ) );
		} );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) {
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
