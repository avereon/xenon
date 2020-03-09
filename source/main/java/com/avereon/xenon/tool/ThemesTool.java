package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ThemeMetadata;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;

public class ThemesTool extends GuidedTool {

	private ComboBox<ThemeMetadata> chooser;

	public ThemesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "themes" ) );
		setTitle( product.rb().text( "tool", "themes-name" ) );

		chooser = new ComboBox<>(  );
		getChildren().add( chooser );

		refreshThemeChooser();
		chooser.getSelectionModel().select( getProgram().getThemeManager().getMetadata( getProgram().getWorkspaceManager().getTheme() ) );
	}

	private void refreshThemeChooser() {
		List<ThemeMetadata> themes = new ArrayList<>( getProgram().getThemeManager().getThemes() );
		themes.sort( null );
		chooser.getItems().clear();
		chooser.getItems().addAll( themes );
	}

}
