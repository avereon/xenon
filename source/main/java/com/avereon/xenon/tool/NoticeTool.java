package com.avereon.xenon.tool;

import com.avereon.data.NodeEvent;
import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticeModel;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.CustomLog;

import java.util.List;

/**
 * The notice tool displays the program notices that have been posted. The tool allows the user to close/dismiss notices or click on the notice to execute a
 * program action.
 */
@CustomLog
public class NoticeTool extends ProgramTool {

	private final VBox noticeContainer;

	private EventHandler<NodeEvent> assetHandler;

	public NoticeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-notice" );

		String clearAllText = Rb.text( BundleKey.TOOL, "notice-clear-all" );
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
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( "tool", "notice-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "notice" ) );
		((NoticeModel)getAssetModel()).register( NodeEvent.NODE_CHANGED, assetHandler = ( e ) -> updateNotices() );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		updateNotices();
	}

	@Override
	protected void display() {
		getProgram().getWorkspaceManager().getActiveWorkspace().hideNotices();
	}

	@Override
	protected void deallocate() {
		((NoticeModel)getAssetModel()).unregister( NodeEvent.NODE_CHANGED, assetHandler );
	}

	private void clearAll() {
		getProgram().getNoticeManager().removeAll();
		this.close();
	}

	private void updateNotices() {
		List<Notice> notices = getProgram().getNoticeManager().getNotices();

		Fx.run( () -> {
			noticeContainer.getChildren().clear();
			for( Notice notice : notices ) {
				NoticePane noticePane = new NoticePane( getProgram(), notice, false );
				noticePane.setOnMouseClicked( ( event ) -> {
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
