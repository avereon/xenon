package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class InfoAreaSettingEditor extends InfoSettingEditor {

	public InfoAreaSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, InfoSettingEditor.Type.AREA );
	}

}
