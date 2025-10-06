package com.avereon.xenon.asset;

import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ResourceTypeCodecAssociationList extends VBox {

	private final ComboBox<Codec.Pattern> associationChoices;

	private final TextField pattern;

	private final Button addButton;

	private final GridPane items;

	@Getter
	private ResourceType resourceType;

	public ResourceTypeCodecAssociationList( XenonProgramProduct product) {
		this.items = new GridPane();
		this.items.setHgap( UiFactory.PAD );
		this.items.setVgap( UiFactory.PAD );

		List<Codec.Pattern> associationPatterns  = Arrays.asList(Codec.Pattern.values());

		// Create the association choices chooser
		associationChoices = new ComboBox<>();
		associationChoices.setItems( FXCollections.observableList( associationPatterns ) );

		// Create the association pattern text field
		pattern = new TextField();
		GridPane.setHgrow( pattern, Priority.ALWAYS );

		// Create the association add button
		this.addButton = new Button( null, product.getProgram().getIconLibrary().getIcon("add") );

		getChildren().setAll( items );

		addButton.setOnAction( e -> {
			// TODO Add the new codec association
		} );
	}

	public void setResourceType( ResourceType resourceType ) {
		this.resourceType = resourceType;
		update();
	}

	private void update() {
		items.getChildren().clear();
		if( resourceType == null ) return;

		// Reload the asset type associations
		int row = 0;

		for( Codec.Association association : resourceType.getAssociations()) {
			items.addRow( row++, new Label(association.pattern().name()), new Label(association.value()) );
		}

		addAdditionRow( row );
	}

	private void addAdditionRow(int row) {
		items.addRow( row, associationChoices, pattern, addButton );
	}

}
