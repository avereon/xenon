package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.tool.settings.Setting;

public class PasswordSettingEditor extends TextLineSettingEditor {

	public PasswordSettingEditor( Product product, Setting setting ) {
		super( product, setting, Type.PASSWORD );
	}

}
