package com.xeomar.xenon.tool;

import com.xeomar.xenon.ProductTool;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.worktool.ToolException;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeTool extends ProductTool {

	private static final Logger log = LoggerFactory.getLogger( WelcomeTool.class );

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double ICON_SIZE = 64;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-welcome" );
		setTitle( product.getResourceBundle().getString( "tool", "welcome-name" ) );

		Node icon = ((Program)product).getIconLibrary().getIcon( "program", ICON_SIZE );

		Label label = new Label( product.getMetadata().getName(), icon );
		label.setFont( new Font( label.getFont().getSize() * 4 ) );
		label.setPadding( new Insets( PAD, PAD, PAD, PAD ) );

		Ellipse slope = new Ellipse( 0, ICON_SIZE + 2 * PAD + SLOPE_RADIUS, SLOPE_RADIUS, SLOPE_RADIUS );
		slope.getStyleClass().add( "accent" );

		AnchorPane anchorPane = new AnchorPane();
		anchorPane.getChildren().addAll( label, slope );
		AnchorPane.setTopAnchor( label, 0.0 );
		AnchorPane.setLeftAnchor( label, 0.0 );

		getChildren().addAll( anchorPane );
	}

	@Override
	protected void allocate() throws ToolException {
		log.info( "Tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.info( "Tool display" );
		Workpane workpane = getWorkpane();
		if( workpane != null  ) workpane.setMaximizedView( getToolView() );
	}

	@Override
	protected void activate() throws ToolException {
		log.info( "Tool activate" );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.info( "Tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.info( "Tool conceal" );
		Workpane workpane = getWorkpane();
		if( workpane != null && workpane.getMaximizedView() == getToolView() ) workpane.setMaximizedView( null );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.info( "Tool deallocate" );
	}

}
