package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class TextAreaSettingEditor extends TextSettingEditor {

	public TextAreaSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, Type.AREA );
	}

}
