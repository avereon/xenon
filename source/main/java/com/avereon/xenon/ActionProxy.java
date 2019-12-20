package com.avereon.xenon;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.Stack;

/**
 * Associated with menu items and tool bar buttons as a proxy for that item so
 * more than one action can be pushed and pulled from the proxy without loosing
 * what was already registered.
 */
public class ActionProxy implements EventHandler<ActionEvent> {

	public static final int NO_MNEMONIC = -1;

	private String id;

	private StringProperty icon;

	private String name;

	private int mnemonic;

	private StringProperty mnemonicName;

	private String type;

	private String shortcut;

	private Stack<Action> actionStack;

	private BooleanProperty enabledProperty;

	public ActionProxy() {
		mnemonic = NO_MNEMONIC;
		icon = new SimpleStringProperty();
		mnemonicName = new SimpleStringProperty();
		enabledProperty = new SimpleBooleanProperty();
		actionStack = new Stack<>();
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getIcon() {
		return icon.getValue();
	}

	public void setIcon( String icon ) {
		this.icon.setValue( icon );
	}

	public StringProperty iconProperty() {
		return icon;
	}

	/**
	 * Get the action name. This name does not have the mnemonic character.
	 *
	 * @return The action name without the mnemonic character
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the action name. This name should not have the mnemonic character.
	 *
	 * @param name The human readable name of the action
	 */
	public void setName( String name ) {
		this.name = name;
		updateMnemonicName();
	}

	/**
	 * Get the action name with the mnemonic character.
	 *
	 * @return The action name with the mnemonic character
	 */
	public String getMnemonicName() {
		return mnemonicName.get();
	}

	public ReadOnlyStringProperty mnemonicNameProperty() {
		return mnemonicName;
	}

	public int getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic( int mnemonic ) {
		this.mnemonic = mnemonic;
		updateMnemonicName();
	}

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public String getShortcut() {
		return shortcut;
	}

	public void setShortcut( String shortcut ) {
		this.shortcut = shortcut;
	}

	public boolean isEnabled() {
		return enabledProperty.get();
	}

	public void setEnabled( boolean enabled ) {
		enabledProperty.set( enabled );
	}

	public ReadOnlyBooleanProperty enabledProperty() {
		return enabledProperty;
	}

	@Override
	public void handle( ActionEvent event ) {
		if( actionStack.size() == 0 ) return;
		actionStack.peek().handle( event );
	}

	public void pushAction( Action action ) {
		pullAction( action );
		actionStack.push( action );
		action.setActionProxy( this );
		updateEnabled();
	}

	public void pullAction( Action action ) {
		action.setActionProxy( null );
		actionStack.remove( action );
		updateEnabled();
	}

	private void updateEnabled() {
		setEnabled( actionStack.size() > 0 && actionStack.peek().isEnabled() );
	}

	private void updateMnemonicName() {
		if( name == null ) {
			mnemonicName.set( null );
		} else if( mnemonic < 0 ) {
			mnemonicName.set( name );
		} else {
			int index = mnemonic;
			mnemonicName.set( name.substring( 0, index ) + "_" + name.substring( index ) );
		}
	}

}
