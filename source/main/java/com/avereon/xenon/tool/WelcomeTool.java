package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import org.tbee.javafx.scene.layout.MigPane;

public class WelcomeTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double ICON_SIZE = 96;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-welcome" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "welcome" ) );
		setTitle( product.rb().text( "tool", "welcome-name" ) );

		Node icon = ((Program)product).getIconLibrary().getIcon( "program", ICON_SIZE );

		Label label = new Label( product.getCard().getName(), icon );
		label.getStyleClass().add( "tool-welcome-title" );

		Ellipse accent = new Ellipse( 0, ICON_SIZE + 2 * PAD + SLOPE_RADIUS, SLOPE_RADIUS, SLOPE_RADIUS );
		accent.getStyleClass().add( "accent" );

		Pane accentPane = new Pane();
		accentPane.getChildren().addAll( accent );

		MigPane contentPane = new MigPane();
		contentPane.add( icon, "spany, aligny top" );
		contentPane.add( label );

		StackPane stack = new StackPane();
		stack.getChildren().addAll( accentPane, contentPane );

		getChildren().addAll( stack );
	}

	@Override
	protected void display() {
		getWorkpane().setMaximizedView( getToolView() );
	}

	@Override
	protected void conceal() {
		if( getToolView().isMaximized() ) getWorkpane().setMaximizedView( null );
	}

}
