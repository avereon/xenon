package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class TextAreaSettingEditor extends TextSettingEditor {

	public TextAreaSettingEditor( ProgramProduct product, String bundleKey, SettingData setting ) {
		super( product, bundleKey, setting, Type.AREA );
	}

}
