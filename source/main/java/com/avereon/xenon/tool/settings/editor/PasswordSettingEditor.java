package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class PasswordSettingEditor extends TextSettingEditor {

	public PasswordSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, Type.PASSWORD );
	}

}
