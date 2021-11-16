package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class PasswordSettingEditor extends TextSettingEditor {

	public PasswordSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, Type.PASSWORD );
	}

}
