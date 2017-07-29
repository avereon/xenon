package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;

public class WelcomeTool extends ProductTool {

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double ICON_SIZE = 64;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-welcome" );
		setTitle( product.getResourceBundle().getString( "tool", "welcome-name" ) );

		Node icon = ((Program)product).getIconLibrary().getIcon( "redo", ICON_SIZE );

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

}
