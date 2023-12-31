package com.avereon.xenon.tool.settings.mediatype;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.Collection;

/**
 * The MediaTypeEditor is not a typical setting editor. It is a more complex
 * editor intended to maintain the relationship between media types and tools.
 */
public class MediaTypeEditor extends SettingEditor {

	public MediaTypeEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {

	}

	@Override
	protected Collection<Node> getComponents() {
		return null;
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {

	}
}
