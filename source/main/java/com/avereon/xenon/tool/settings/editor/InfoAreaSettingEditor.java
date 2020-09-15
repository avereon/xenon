package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;

public class InfoAreaSettingEditor extends InfoLineSettingEditor {

	public InfoAreaSettingEditor( ProgramProduct product, String bundleKey, Setting setting ) {
		super( product, bundleKey, setting, InfoLineSettingEditor.Type.AREA );
	}

}
