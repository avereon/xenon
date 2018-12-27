package com.xeomar.xenon.tool.notice;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.notice.Notice;
import com.xeomar.xenon.notice.NoticePane;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;
import com.xeomar.xenon.resource.ResourceListener;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import com.xeomar.xenon.workarea.Workpane;
import javafx.application.Platform;
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
		setTitle( product.getResourceBundle().getString( "tool", "notice-name" ) );

		noticeContainer = new VBox();
		getChildren().addAll( noticeContainer );

		updateNotices();

		resourceWatcher = new ResourceWatcher();
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );

		getResource().addResourceListener( resourceWatcher );
	}

	private void updateNotices() {
		List<Notice> notices = getProgram().getNoticeManager().getNotices();

		Platform.runLater( () -> {
			noticeContainer.getChildren().clear();
			for( Notice notice : notices ) {
				NoticePane noticePane = new NoticePane( getProgram(), notice );
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
				case SAVED: {
					updateNotices();
					break;
				}
			}
		}

	}

}
