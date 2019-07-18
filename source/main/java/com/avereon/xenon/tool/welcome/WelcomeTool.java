package com.avereon.xenon.tool.welcome;

import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.ToolException;
import com.avereon.xenon.workarea.Workpane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import org.slf4j.Logger;
import org.tbee.javafx.scene.layout.MigPane;

import java.lang.invoke.MethodHandles;

public class WelcomeTool extends ProgramTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double ICON_SIZE = 96;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-welcome" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "welcome" ) );
		setTitle( product.getResourceBundle().getString( "tool", "welcome-name" ) );

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
	protected void allocate() throws ToolException {
		log.debug( "Tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Tool display" );
		Workpane workpane = getWorkpane();
		if( workpane != null ) workpane.setMaximizedView( getToolView() );
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Tool activate" );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Tool conceal" );
		Workpane workpane = getWorkpane();
		if( workpane != null && workpane.getMaximizedView() == getToolView() ) workpane.setMaximizedView( null );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Tool deallocate" );
	}

}
