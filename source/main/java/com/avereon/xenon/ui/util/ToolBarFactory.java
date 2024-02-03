package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import com.avereon.zarra.javafx.FxUtil;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import java.util.List;

public class ToolBarFactory extends NavFactory {

	public static final String TOOL_ITEM_ID_PREFIX = "toolitem-";

	public static ToolBar createToolBar( Xenon program ) {
		return createToolBar( program, "" );
	}

	public static ToolBar createToolBar( Xenon program, String descriptor ) {
		ToolBar toolbar = new ToolBar();
		List<Token> tokens = parseDescriptor( descriptor );
		toolbar.getItems().addAll( tokens.stream().map( t -> createToolBarItem( program, toolbar, t ) ).toList() );
		return toolbar;
	}

	public static Button createToolBarButton( Xenon program, String id ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( id ) );
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

	private static Node createToolBarItem( Xenon program, Node parent, Token item ) {
		if( item.isSeparator() ) {
			return new Separator();
		} else if( item.getChildren().isEmpty() ) {
			return createToolBarButton( program, item );
		} else {
			return createToolTray( program, parent, item );
		}
	}

	private static Button createToolTray( Xenon program, Node parent, Token item ) {
		Popup popup = new Popup();
		popup.setAutoFix( true );
		popup.setAutoHide( true );

		ToolBar tray = new ToolBar();
		tray.getStyleClass().add( "toolbar-tray" );
		tray.getItems().addAll( item.getChildren().stream().map( t -> createToolBarItem( program, tray, t ) ).toList() );
		tray.setOrientation( rotate( ((ToolBar)parent).getOrientation() ) );
		((ToolBar)parent).orientationProperty().addListener( ( p, o, n ) -> tray.setOrientation( rotate( ((ToolBar)parent).getOrientation() ) ) );

		popup.getContent().add( tray );

		Button button = createToolBarButton( program, item );
		button.getStyleClass().add( "toolbar-tray-trigger-button" );
		button.setOnAction( ( e ) -> doToggleTrayDialog( button, popup, tray ) );
		button.setDisable( false );

		return button;
	}

	private static Orientation rotate( Orientation orientation ) {
		if( orientation == Orientation.HORIZONTAL ) return Orientation.VERTICAL;
		return Orientation.HORIZONTAL;
	}

	private static void doToggleTrayDialog( Button button, Popup popup, ToolBar tray ) {
		if( !popup.isShowing() ) {
			// Initially show the tray off-screen, so it can be laid out before the tray offset is calculated
			popup.show( button, Double.MIN_VALUE, Double.MIN_VALUE );

			// Calculate offset after the tray is shown so the tray
			double offset = FxUtil.localToParent( tray.getItems().get( 0 ), button ).getMinX();
			Point2D anchor = button.localToScreen( new Point2D( -offset, button.getHeight() ) );

			// Move the popup to the correct location
			popup.setX( anchor.getX() );
			popup.setY( anchor.getY() );
		} else {
			popup.hide();
		}
	}

	private static Button createToolBarButton( Xenon program, Token token ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( token.getId() ) );
	}

	private static Button createToolBarButton( Xenon program, ActionProxy action ) {
		Button button = new Button();
		button.setId( TOOL_ITEM_ID_PREFIX + action.getId() );
		button.setOnAction( action );
		button.setDisable( !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		action.enabledProperty().addListener( ( event ) -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( ( event ) -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return button;
	}

}
