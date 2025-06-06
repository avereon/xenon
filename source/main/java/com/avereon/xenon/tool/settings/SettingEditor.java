package com.avereon.xenon.tool.settings;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.editor.*;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SettingEditor {

	private static final Map<String, Class<? extends SettingEditor>> editors;

	protected final XenonProgramProduct product;

	@Getter
	protected final SettingData setting;

	@Getter
	protected final String rbKey;

	static {
		editors = new ConcurrentHashMap<>();

		// TODO Add toggle setting editor

		addType( "textline", TextLineSettingEditor.class );
		addType( "textarea", TextAreaSettingEditor.class );
		addType( "password", PasswordSettingEditor.class );
		addType( "checkbox", CheckBoxSettingEditor.class );
		addType( "combobox", ComboBoxSettingEditor.class );
		addType( "infoline", InfoLineSettingEditor.class );
		addType( "infoarea", InfoAreaSettingEditor.class );
		addType( "color", ColorSettingEditor.class );
		addType( "paint", PaintSettingEditor.class );
		addType( "file", FileSettingEditor.class );
		addType( "folder", FolderSettingEditor.class );
		addType( "font", FontSettingEditor.class );
		//		//		addType( "link", LinkSettingEditor.class );
		//		//		addType( "time", TimeSettingEditor.class );
		addType( "update-checks", UpdateSettingViewer.class );
	}

	public SettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( setting == null ) throw new NullPointerException( "Setting cannot be null" );
		this.product = product;
		this.rbKey = rbKey;
		this.setting = setting;
	}

	protected XenonProgramProduct getProduct() {
		return product;
	}

	public String getKey() {
		return setting.getKey();
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

	/**
	 * <p>
	 * Add components to the editor. This is done by adding a row to the specified
	 * pane with the components for this editor. Components include the label and
	 * other components like text fields, comboboxes, checkboxes, etc.
	 * </p>
	 * <p>
	 * Example:
	 * </p>
	 * <pre>
	 * pane.addRow( row, label, field );
	 * </pre>
	 *
	 * @param pane The pane to add components to
	 * @param row The row that is being configured
	 */
	public abstract void addComponents( GridPane pane, int row );

	/**
	 * Get the components in the editor. This should return the set of all
	 * components for the editor including the label and any other components.
	 * This method is used to manage visibility states for the editor.
	 *
	 * @return All the components for this editor
	 */
	protected abstract Collection<Node> getComponents();

	/**
	 * Called with the setting value changes. Use this method to change editor
	 * values when the setting value changes.
	 *
	 * @param event The setting change event
	 */
	protected abstract void doSettingValueChanged( SettingsEvent event );

	/**
	 * Called when the setting value changes in the SettingsPage.
	 */
	protected abstract void pageSettingsChanged();

	public void setDisable( boolean disable ) {
		Fx.run( () -> getComponents().forEach( n -> n.setDisable( disable ) ) );
	}

	public void setVisible( boolean visible ) {
		Fx.run( () -> getComponents().forEach( n -> {
			n.setVisible( visible );
			n.setManaged( visible );
		} ) );
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T getCurrentValue() {
		return (T)setting.getSettings().get( getKey() );
	}

	// Setting listener

	final void handle( SettingsEvent event ) {
		doSettingValueChanged( event );
	}

}
