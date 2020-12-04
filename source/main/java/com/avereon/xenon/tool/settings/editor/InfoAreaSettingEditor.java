package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;

public class InfoAreaSettingEditor extends InfoSettingEditor {

	public InfoAreaSettingEditor( ProgramProduct product, String bundleKey, Setting setting ) {
		super( product, bundleKey, setting, InfoSettingEditor.Type.AREA );
	}

}
