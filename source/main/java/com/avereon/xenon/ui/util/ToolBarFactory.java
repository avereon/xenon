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

import java.util.List;
import java.util.stream.Collectors;

public class ToolBarFactory extends BarFactory {

	public static ToolBar createToolBar( Program program, String descriptor ) {
		ToolBar toolbar = new ToolBar();
		List<Token> tokens = parseDescriptor( descriptor );
		toolbar.getItems().addAll( tokens.stream().map( t -> createToolBarItem( program, t ) ).collect( Collectors.toList() ) );
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

	private static Node createToolBarItem( Program program, Token item ) {
		if( item.isSeparator() ) {
			return new Separator();
		} else if( item.getChildren().isEmpty() ) {
			return createToolBarButton( program, item.getId() );
		} else {
			// TODO Create a button that has a "popup" toolbar
			// Bind the orientation to the parent orientation
			//toolbar.getItems().add( createToolBar( program, createToolBarItem( program, item ) ) );
			return createToolBarButton( program, item.getId() );
		}
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
