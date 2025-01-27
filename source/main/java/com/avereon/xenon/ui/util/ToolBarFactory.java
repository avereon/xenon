package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import lombok.CustomLog;

import java.util.List;

@CustomLog
public class ToolBarFactory extends NavFactory {

	public static final String TOOL_ITEM_ID_PREFIX = "toolitem-";

	public static ToolBar createToolBar( Xenon program ) {
		return createToolBar( program, "" );
	}

	public static ToolBar createToolBar( Xenon program, String descriptor ) {
		ToolBar toolbar = new ToolBar();
		List<Token> tokens = parseDescriptor( descriptor );
		toolbar.getItems().addAll( tokens.stream().map( t -> createToolBarItem( program, toolbar, t, null ) ).toList() );
		return toolbar;
	}

	public static Region createSpring() {
		Region spring = new Region();
		HBox.setHgrow( spring, Priority.ALWAYS );
		return spring;
	}

	public static Region createPad() {
		Region pad = new Region();
		pad.setMinSize( UiFactory.PAD, UiFactory.PAD );
		return pad;
	}

	public static Button createToolBarButton( Xenon program, String id ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( id ), null );
	}

	private static Node createToolBarItem( Xenon program, ToolBar toolbar, Token item, PopupWindow popup ) {
		if( item.getType() == Token.Type.SEPARATOR ) {
			return new Separator();
		} else if( item.getChildren().isEmpty() ) {
			return createToolBarButton( program, item, popup );
		} else if( item.getType() == Token.Type.TRAY ) {
			return createToolTray( program, toolbar, item );
		} else if( item.getType() == Token.Type.MENU ) {
			return createToolMenu( program, toolbar, item );
		}

		log.atError().log( "Unknown toolbar item type: %s", item );

		return ToolBarFactory.createPad();
	}

	private static Button createToolTray( Xenon program, ToolBar toolbar, Token item ) {
		Popup popup = new Popup();
		popup.setAutoFix( true );
		popup.setAutoHide( true );
		popup.setHideOnEscape( true );

		ToolBar tray = new ToolBar();
		tray.setId( TRAY_PREFIX + item.getId() );
		tray.getStyleClass().add( "action-tray" );
		tray.getItems().addAll( item.getChildren().stream().map( t -> createToolBarItem( program, tray, t, popup ) ).toList() );
		tray.setOrientation( rotate( toolbar.getOrientation() ) );
		toolbar.orientationProperty().addListener( ( p, o, n ) -> tray.setOrientation( rotate( toolbar.getOrientation() ) ) );

		popup.getContent().add( tray );

		Button button = createToolBarButton( program, item, null );
		button.getStyleClass().add( "toolbar-tray-trigger-button" );
		button.setOnAction( ( e ) -> doToggleTrayDialog( button, popup, tray.getItems().getFirst() ) );
		button.setDisable( false );

		return button;
	}

	private static Button createToolMenu( Xenon program, ToolBar toolbar, Token item ) {
		ContextMenu menu = MenuBarFactory.createContextMenu( program, item, true );
		menu.getStyleClass().add( "action-tray" );

		Button button = createToolBarButton( program, item, null );
		button.getStyleClass().add( "toolbar-tray-trigger-button" );
		button.setOnAction( ( e ) -> doToggleTrayDialog( button, menu, menu.getItems().getFirst() ) );
		button.setDisable( false );

		return button;
	}

	private static Orientation rotate( Orientation orientation ) {
		if( orientation == Orientation.HORIZONTAL ) return Orientation.VERTICAL;
		return Orientation.HORIZONTAL;
	}

	private static void doToggleTrayDialog( Button button, PopupWindow popup, Object alignmentChild ) {
		if( !popup.isShowing() ) {
			// Initially show the tray off-screen, so it can be laid out before the tray offset is calculated
			popup.show( button, Double.MIN_VALUE, Double.MIN_VALUE );

			// Calculate offset for the tray so it will be aligned with the button
			Point2D anchor = button.localToScreen( 0, button.getHeight() );

			// FIXME This strategy to fix the tray alignment does not work anymore
			//  because the tray is not a child of the button
//			if( alignmentChild instanceof MenuItem item ) {
//				Bounds buttonScreenBounds = button.localToScreen( button.getBoundsInLocal() );
//				Bounds itemScreenBounds = item.getGraphic().localToScreen( item.getGraphic().getBoundsInLocal() );
//
//				//offset = FxUtil.localToParent( item.getGraphic(), button ).getMinX();
//				offset = buttonScreenBounds.getMinX() - itemScreenBounds.getMinX();
//			} else if( alignmentChild instanceof Node node ) {
			// TODO Take into account the padding around the toolbar
//				// Use the parent layout bounds to calculate the offset
//				//offset = node.localToParent( node.getLayoutX(), node.getLayoutY() ).getX();
//				//Bounds bounds = node.localToParent( node.getBoundsInLocal() );
//				Bounds bounds = FxUtil.localToAncestor( node, button );
//
//				offset = FxUtil.localToAncestor( node, button ).getMinX();
//				anchor = button.localToScreen( new Point2D( -offset, button.getLayoutY() + button.getHeight() ) );
//			}

			//Point2D anchor = button.localToScreen( new Point2D( -offset, button.getHeight() ) );

			// Move the popup to the correct location
			popup.setX( anchor.getX() );
			popup.setY( anchor.getY() );
		} else {
			popup.hide();
		}
	}

	private static Button createToolBarButton( Xenon program, Token token ) {
		return createToolBarButton( program, token, null );
	}

	private static Button createToolBarButton( Xenon program, Token token, PopupWindow popup ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( token.getId() ), popup );
	}

	private static Button createToolBarButton( Xenon program, ActionProxy action, PopupWindow popup ) {
		Button button = new Button();
		button.setId( TOOL_ITEM_ID_PREFIX + action.getId() );
		button.setOnAction( action );
		button.setDisable( !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		action.enabledProperty().addListener( ( event ) -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( ( event ) -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		if( popup != null ) button.addEventHandler( MouseEvent.MOUSE_CLICKED, ( e ) -> popup.hide() );

		return button;
	}

}
