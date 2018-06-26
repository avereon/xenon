package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.update.MarketCard;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.tbee.javafx.scene.layout.MigPane;

class MarketPane extends MigPane {

	private ProductTool productTool;

	private MarketCard source;

	private Label iconLabel;

	private Label nameLabel;

	private Label uriLabel;

	private Button enableButton;

	private Button removeButton;

	public MarketPane( ProductTool productTool, MarketCard source ) {
		super( "insets 0, gap " + UiFactory.PAD );

		this.productTool = productTool;
		this.source = source;

		setId( "tool-product-market" );

		Program program = productTool.getProgram();

		String iconUri = source.getIconUri();
		Node marketIcon = program.getIconLibrary().getIcon( "market", ProductTool.ICON_SIZE );
		//Node marketIcon = program.getIconLibrary().getIcon( iconUri, ProductTool.ICON_SIZE );

		iconLabel = new Label( null, marketIcon );
		iconLabel.setId( "tool-product-market-icon" );
		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-market-name" );
		uriLabel = new Label( source.getCardUri() );
		uriLabel.setId( "tool-product-market-uri" );

		enableButton = new Button( "", productTool.getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
		removeButton = new Button( "", program.getIconLibrary().getIcon( "remove" ) );

		add( iconLabel, "spany, aligny center" );
		add( nameLabel, "pushx" );
		add( removeButton );
		add( uriLabel, "newline" );
		add( enableButton );
	}

	MarketCard getSource() {
		return source;
	}

	void updateMarketState() {
		// TODO Update the market state
		enableButton.setGraphic( productTool.getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
		enableButton.setDisable( !source.isRemovable() );
		removeButton.setDisable( !source.isRemovable() );
	}

}
