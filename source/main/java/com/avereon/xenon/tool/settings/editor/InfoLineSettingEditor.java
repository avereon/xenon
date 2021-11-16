package com.avereon.xenon.tool.settings.editor;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;

public class InfoLineSettingEditor extends InfoSettingEditor {

	public InfoLineSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting, Type.FIELD );
	}

}
