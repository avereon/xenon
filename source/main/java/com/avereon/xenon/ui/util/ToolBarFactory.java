package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import java.util.List;
import java.util.stream.Collectors;

public class ToolBarFactory extends BarFactory {

	public static ToolBar createToolBar( Program program, String descriptor ) {
		ToolBar toolbar = new ToolBar();
		List<Token> tokens = parseDescriptor( descriptor );
		toolbar.getItems().addAll( tokens.stream().map( t -> createToolBarItem( program, toolbar, t ) ).collect( Collectors.toList() ) );
		return toolbar;
	}

	public static Button createToolBarButton( Program program, String id ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( id ), false );
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

	private static Node createToolBarItem( Program program, Node parent, Token item ) {
		if( item.isSeparator() ) {
			return new Separator();
		} else if( item.getChildren().isEmpty() ) {
			return createToolBarButton( program, item, false );
		} else {
			return createToolTray( program, parent, item );
		}
	}

	private static Button createToolTray( Program program, Node parent, Token item ) {
		ToolBar tray = new ToolBar();
		tray.getItems().addAll( item.getChildren().stream().map( t -> createToolBarItem( program, tray, t ) ).collect( Collectors.toList() ) );
		tray.setOrientation( rotate( ((ToolBar)parent).getOrientation() ) );
		((ToolBar)parent).orientationProperty().addListener( (p,o,n) -> tray.setOrientation( rotate( ((ToolBar)parent).getOrientation() ) ) );

		Popup popup = new Popup();
		popup.getContent().add( tray );

		Button button = createToolBarButton( program, item, true );
		button.getStyleClass().add( "toolbar-tray-trigger-button" );
		button.setOnAction( ( e ) -> doToggleTrayDialog( button, popup, tray ) );

		return button;
	}

	private static Orientation rotate( Orientation orientation ) {
		if( orientation == Orientation.HORIZONTAL ) return Orientation.VERTICAL;
		return Orientation.HORIZONTAL;
	}

	private static void doToggleTrayDialog( Button button, Popup popup, Control tray ) {
		if( !popup.isShowing() ) {
			Point2D anchor = button.localToScreen( new Point2D( 0, button.getHeight() ) );
			popup.show( button, anchor.getX(), anchor.getY() );
		} else {
			popup.hide();
		}
	}

	private static Button createToolBarButton( Program program, Token token, boolean tray ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( token.getId() ), tray );
	}

	private static Button createToolBarButton( Program program, ActionProxy action, boolean tray ) {
		Button button = new Button();

		button.setOnAction( action );
		button.setDisable( !tray && !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		if( !tray ) action.enabledProperty().addListener( ( event ) -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( ( event ) -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return button;
	}

}
