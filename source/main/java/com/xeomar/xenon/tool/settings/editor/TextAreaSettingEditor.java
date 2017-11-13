package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;

public class TextAreaSettingEditor extends TextLineSettingEditor {

	public TextAreaSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting, Type.AREA );
	}

}
