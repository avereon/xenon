package com.xeomar.xenon.workarea;

import com.xeomar.util.LogUtil;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ToolTabSkin extends SkinBase<ToolTab> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "selected" );

	private BorderPane tabLayout;

	private Label label;

	private Button close;

	protected ToolTabSkin( ToolTab tab ) {
		super( tab );

		Tool tool = tab.getTool();

		label = new Label();
		label.getStyleClass().setAll( "tool-tab-label" );
		label.graphicProperty().bind( tool.graphicProperty() );
		label.textProperty().bind( tool.titleProperty() );

		close = new Button( "X" );
		close.getStyleClass().setAll( "tool-tab-close-button" );

		tabLayout = new BorderPane();
		tabLayout.setCenter( label );
		tabLayout.setRight( close );
		tabLayout.getStyleClass().setAll( "tool-tab-container" );

		getChildren().setAll( tabLayout );

		label.setOnMousePressed( ( event ) -> tab.getToolPane().getSelectionModel().select( tab ) );
		close.setOnMouseReleased( ( event ) -> tab.getOnCloseRequest().handle( event ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

}
