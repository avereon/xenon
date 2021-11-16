package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class InfoAreaSettingEditor extends InfoSettingEditor {

	public InfoAreaSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, InfoSettingEditor.Type.AREA );
	}

}
