package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;

public class PasswordSettingEditor extends TextLineSettingEditor {

	public PasswordSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting, Type.PASSWORD );
	}

}
