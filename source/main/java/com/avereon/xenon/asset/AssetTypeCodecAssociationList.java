package com.avereon.xenon.asset;

import com.avereon.xenon.UiFactory;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class AssetTypeCodecAssociationList extends VBox {

	private final GridPane items;

	private final ComboBox<Codec.Pattern> associationChoices;

	private final TextField pattern;

	private final Button addButton;

	@Getter
	private AssetType assetType;

	public AssetTypeCodecAssociationList() {
		this.items = new GridPane();
		this.items.setHgap( UiFactory.PAD );
		this.items.setVgap( UiFactory.PAD );

		List<Codec.Pattern> associationPatterns  = Arrays.asList(Codec.Pattern.values());
		associationChoices = new ComboBox<>();
		associationChoices.setItems( FXCollections.observableList( associationPatterns ) );
		pattern = new TextField();

		this.addButton = new Button( "Add Association" );

		getChildren().setAll( items );

		addButton.setOnAction( e -> {
			// Add the new codec association
		} );
	}

	public void setAssetType( AssetType assetType ) {
		this.assetType = assetType;
		update();
	}

	private void update() {
		items.getChildren().clear();
		if( assetType == null ) return;

		// Reload the asset type associations
		int row = 0;

		for( Codec.Association association : assetType.getAssociations()) {
			items.addRow( row++, new Label(association.pattern().name()), new Label(association.value()) );
		}

		addAdditionRow( row );
	}

	private void addAdditionRow(int row) {
		items.addRow( row, associationChoices, pattern, addButton );
	}

}
