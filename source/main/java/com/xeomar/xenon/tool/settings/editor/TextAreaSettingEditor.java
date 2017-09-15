package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.tool.settings.Setting;

public class TextAreaSettingEditor extends TextLineSettingEditor {

	public TextAreaSettingEditor( Product product, Setting setting ) {
		super( product, setting, Type.AREA );
	}

}
