package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.tool.settings.Setting;

public class InfoAreaSettingEditor extends InfoLineSettingEditor {

	public InfoAreaSettingEditor( Product product, Setting setting ) {
		super( product, setting, InfoLineSettingEditor.Type.AREA );
	}

}
