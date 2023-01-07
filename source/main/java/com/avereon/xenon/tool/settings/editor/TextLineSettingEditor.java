package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class TextLineSettingEditor extends TextSettingEditor {

	public TextLineSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, Type.FIELD );
	}

}
