package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;

public class PasswordSettingEditor extends TextLineSettingEditor {

	public PasswordSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting, Type.PASSWORD );
	}

}
