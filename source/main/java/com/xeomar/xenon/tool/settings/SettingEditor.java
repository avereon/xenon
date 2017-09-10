package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.settings.SettingsListener;
import com.xeomar.xenon.tool.settings.editor.CheckboxSettingEditor;
import com.xeomar.xenon.tool.settings.editor.PasswordSettingEditor;
import com.xeomar.xenon.tool.settings.editor.TextSettingEditor;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;

public abstract class SettingEditor implements SettingsListener {

	private static Map<String, Class<? extends SettingEditor>> editors;

	protected Product product;

	protected Setting setting;

	protected String key;

	static {
		editors = new HashMap<>();

		addType( "text", TextSettingEditor.class );
		addType( "password", PasswordSettingEditor.class );
		addType( "checkbox", CheckboxSettingEditor.class );
//		addType( "combobox", ComboboxSettingEditor.class );
//		addType( "infoline", InfolineSettingEditor.class );
//		addType( "infoarea", InfoareaSettingEditor.class );
//		addType( "color", ColorSettingEditor.class );
//		addType( "file", FileSettingEditor.class );
//		addType( "font", FontSettingEditor.class );
//		//		addType( "link", LinkSettingEditor.class );
//		//		addType( "time", TimeSettingEditor.class );
	}

	public SettingEditor( Product product, Setting setting ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( setting == null ) throw new NullPointerException( "Setting cannot be null" );
		this.product = product;
		this.setting = setting;
		this.key=setting.getKey();
	}

	/**
	 * Register a new setting editor.
	 *
	 * @param key The editor key
	 * @param editor The setting editor
	 */
	public static void addType( String key, Class<? extends SettingEditor> editor ) {
		editors.putIfAbsent( key, editor );
	}

	public static Class<? extends SettingEditor> getType( String key ) {
		return editors.get( key );
	}

	public abstract void addComponents( GridPane pane, int row );

	public abstract void setEnabled( boolean enabled );

	public abstract void setVisible( boolean visible );

	public Settings getSettings() {
		return setting.getSettings();
	}

}
