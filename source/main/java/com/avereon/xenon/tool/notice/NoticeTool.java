package com.avereon.xenon.tool.notice;

import com.avereon.util.LogUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;
import com.avereon.xenon.resource.ResourceListener;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.ToolException;
import com.avereon.xenon.OpenToolRequestParameters;
import com.avereon.xenon.workarea.Workpane;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * The notice tool displays the program notices that have been posted. The tool allows the user to close/dismiss notices or click on the notice to execute a program action.
 */
public class NoticeTool extends ProgramTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ResourceWatcher resourceWatcher;

	private VBox noticeContainer;

	public NoticeTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-notice" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "notice" ) );
		setTitle( product.rb().text( "tool", "notice-name" ) );

		String clearAllText = product.rb().text( BundleKey.TOOL, "notice-clear-all" );
		Button clearAllButton = new Button( clearAllText );
		clearAllButton.getStyleClass().addAll( "padded" );
		clearAllButton.setOnMouseClicked( ( event ) -> this.clearAll() );

		VBox buttonBox = new VBox( clearAllButton );
		buttonBox.getStyleClass().addAll( "padded", "notice-buttons" );

		ScrollPane scroller = new ScrollPane( noticeContainer = new VBox() );
		scroller.setFitToWidth( true );
		scroller.setFitToHeight( true );

		BorderPane layout = new BorderPane( scroller, buttonBox, null, null, null );
		clearAllButton.prefWidthProperty().bind( layout.widthProperty() );

		getChildren().addAll( layout );
		updateNotices();

		resourceWatcher = new ResourceWatcher();
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	protected void resourceReady( OpenToolRequestParameters parameters ) throws ToolException {
		super.resourceReady( parameters );

		getResource().addResourceListener( resourceWatcher );
	}

	@Override
	protected void display() {
		getProgram().getWorkspaceManager().getActiveWorkspace().hideNotices();
	}

	private void clearAll() {
		getProgram().getNoticeManager().removeAll();
		this.close();
	}

	private void updateNotices() {
		List<Notice> notices = getProgram().getNoticeManager().getNotices();

		Platform.runLater( () -> {
			noticeContainer.getChildren().clear();
			for( Notice notice : notices ) {
				NoticePane noticePane = new NoticePane( getProgram(), notice, false );
				noticePane.onMouseClickedProperty().set( (event)->{
					noticePane.executeNoticeAction();
					event.consume();
					this.close();
				} );
				noticePane.getCloseButton().onMouseClickedProperty().set( ( event ) -> {
					getProgram().getNoticeManager().removeNotice( notice );
					event.consume();
				} );
				noticeContainer.getChildren().add( noticePane );
			}
		} );
	}

	private class ResourceWatcher implements ResourceListener {

		@Override
		public void eventOccurred( ResourceEvent event ) {
			switch( event.getType() ) {
				case REFRESHED: {
					updateNotices();
					break;
				}
			}
		}

	}

}
