package com.xeomar.xenon.workarea;

import com.xeomar.util.LogUtil;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ToolTabSkin extends SkinBase<ToolTab> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "selected" );

	private BorderPane tabContainer;

	private Label label;

	private Button close;

	protected ToolTabSkin( ToolTab tab ) {
		super( tab );

		Tool tool = tab.getTool();

		this.label = new Label();
		this.label.getStyleClass().setAll( "tool-tab-label" );
		this.label.graphicProperty().bind( tool.graphicProperty() );
		this.label.textProperty().bind( tool.titleProperty() );

		this.close = new Button( "X" );
		this.close.getStyleClass().setAll( "tool-tab-close-button" );

		tabContainer = new BorderPane();
		tabContainer.setCenter( label );
		tabContainer.setRight( close );

		// FIXME This way of doing it does not work;
		tab.getChildrenUnmodifiable().setAll( tabContainer );

		label.setOnMousePressed( ( event ) -> tab.toolPaneProperty().get().getSelectionModel().select( tab ) );
		close.setOnMouseReleased( ( event ) -> tab.getOnCloseRequest().handle( event ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		//super.layoutChildren( contentX, contentY, contentWidth, contentHeight );
		log.info( "Layout ToolTab children" );
	}

}
