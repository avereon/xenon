package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;

public class TextAreaSettingEditor extends TextLineSettingEditor {

	public TextAreaSettingEditor( ProgramProduct product, String bundleKey, Setting setting ) {
		super( product, bundleKey, setting, Type.AREA );
	}

}
