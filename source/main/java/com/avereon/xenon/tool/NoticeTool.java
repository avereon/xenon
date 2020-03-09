package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workpane.Workpane;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The notice tool displays the program notices that have been posted. The tool allows the user to close/dismiss notices or click on the notice to execute a program action.
 */
public class NoticeTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private VBox noticeContainer;

	public NoticeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
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
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	protected void assetRefreshed() throws ToolException {
		super.assetRefreshed();
		updateNotices();
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		updateNotices();
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
				noticePane.setOnMouseClicked( (event)->{
					noticePane.executeNoticeAction();
					event.consume();
					this.close();
				} );
				noticePane.getCloseButton().setOnMouseClicked( ( event ) -> {
					getProgram().getNoticeManager().removeNotice( notice );
					event.consume();
				} );
				noticeContainer.getChildren().add( noticePane );
			}
		} );
	}

}
