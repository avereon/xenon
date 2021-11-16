package com.avereon.xenon.tool.settings;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.editor.*;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SettingEditor {

	private static final Map<String, Class<? extends SettingEditor>> editors;

	protected final ProgramProduct product;

	protected final SettingData setting;

	protected final String rbKey;

	static {
		editors = new HashMap<>();

		addType( "textline", TextLineSettingEditor.class );
		addType( "textarea", TextAreaSettingEditor.class );
		addType( "password", PasswordSettingEditor.class );
		addType( "checkbox", CheckBoxSettingEditor.class );
		addType( "combobox", ComboBoxSettingEditor.class );
		addType( "infoline", InfoLineSettingEditor.class );
		addType( "infoarea", InfoAreaSettingEditor.class );
		addType( "color", ColorSettingEditor.class );
		addType( "file", FileSettingEditor.class );
		addType( "folder", FolderSettingEditor.class );
		addType( "font", FontSettingEditor.class );
		//		//		addType( "link", LinkSettingEditor.class );
		addType( "paint", PaintSettingEditor.class );
		//		//		addType( "time", TimeSettingEditor.class );
		addType( "update-checks", UpdateSettingViewer.class );
	}

	public SettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( setting == null ) throw new NullPointerException( "Setting cannot be null" );
		this.product = product;
		this.rbKey = rbKey;
		this.setting = setting;
	}

	protected ProgramProduct getProduct() {
		return product;
	}

	public String getRbKey() {
		return rbKey;
	}

	public SettingData getSetting() {
		return setting;
	}

	public String getKey() {
		return setting.getKey();
	}

	/**
	 * Register a new setting editor.
	 *
	 * @param key    The editor key
	 * @param editor The setting editor
	 */
	public static void addType( String key, Class<? extends SettingEditor> editor ) {
		editors.putIfAbsent( key, editor );
	}

	public static Class<? extends SettingEditor> getType( String key ) {
		return editors.get( key );
	}

	public abstract void addComponents( GridPane pane, int row );

	public abstract List<Node> getComponents();

	protected abstract void doSettingValueChanged(SettingsEvent event);

	public void setDisable( boolean disable ) {
		getComponents().forEach( n -> n.setDisable( disable ) );
	}

	public void setVisible( boolean visible ) {
		getComponents().forEach( n -> {
			n.setVisible( visible );
			n.setManaged( visible );
		} );
	}
	// Setting listener

	public void handle( SettingsEvent event ) {
		doSettingValueChanged(event);
	}

}
