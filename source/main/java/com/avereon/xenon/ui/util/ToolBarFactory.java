package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

	private static Node createToolBarItem( Program program, ToolBar parent, Token item ) {
		if( item.isSeparator() ) {
			return new Separator();
		} else if( item.getChildren().isEmpty() ) {
			return createToolBarButton( program, item );
		} else {
			return createToolTray( program, parent, item );
		}
	}

	private static Button createToolTray( Program program, ToolBar parent, Token item ) {
		Button button = createToolBarButton( program, item );

		ToolBar tray = new ToolBar();
		tray.getItems().addAll( item.getChildren().stream().map( t -> createToolBarItem( program, tray, t ) ).collect( Collectors.toList() ) );
		Popup popup = new Popup();
		popup.getContent().add( tray );


		// TODO Create a button that has a "popup" toolbar
		// Bind the orientation to the parent orientation
		//toolbar.getItems().add( createToolBar( program, createToolBarItem( program, item ) ) );

		return button;
	}

	private static Button createToolBarButton( Program program, Token token ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( token.getId() ) );
	}

	private static Button createToolBarButton( Program program, ActionProxy action ) {
		Button button = new Button();

		button.setOnAction( action );
		button.setDisable( !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		action.enabledProperty().addListener( ( event ) -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( ( event ) -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return button;
	}

}
