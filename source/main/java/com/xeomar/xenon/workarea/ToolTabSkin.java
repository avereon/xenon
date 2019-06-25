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

		close = new Button();
		close.graphicProperty().bind( tool.closeGraphicProperty() );
		close.getStyleClass().setAll( "tool-tab-close-button" );

		tabLayout = new BorderPane();
		tabLayout.setCenter( label );
		tabLayout.setRight( close );
		tabLayout.getStyleClass().setAll( "tool-tab-container" );

		tab.getStyleClass().add( "tool-tab" );

		getChildren().setAll( tabLayout );

		label.setOnMousePressed( ( event ) -> {
			select( tab );
			if( event.getClickCount() == 2 ) {
				WorkpaneView view = tool.getToolView();
				view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
			}
		} );
		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
		tab.setOnCloseRequest( event -> {
			event.consume();
			tool.close();
		} );

		boolean selected = tab.getToolPane().getSelectionModel().getSelectedItem() == tab;
		pseudoClassStateChanged( SELECTED_PSEUDOCLASS_STATE, selected );

		tab.selectedProperty().addListener( ( event ) -> pseudoClassStateChanged( SELECTED_PSEUDOCLASS_STATE, tab.isSelected() ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

	private void select( ToolTab tab ) {
		log.warn( "select: " + tab.getTool().getTitle() );
		tab.getToolPane().getSelectionModel().select( tab );
		tab.getToolPane().requestFocus();
	}

}
