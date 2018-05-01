package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;

public class InfoAreaSettingEditor extends InfoLineSettingEditor {

	public InfoAreaSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting, InfoLineSettingEditor.Type.AREA );
	}

}
