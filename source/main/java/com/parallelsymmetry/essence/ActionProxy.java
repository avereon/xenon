package com.parallelsymmetry.essence;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.Stack;

/**
 * Associated with menu items and tool bar buttons as a proxy for that item
 * so more than one action can be pushed and pulled from the proxy without
 * loosing what was already registered.
 *
 * @param <T>
 */
public class ActionProxy<T extends ActionEvent> implements EventHandler<T> {

	public static final int NO_MNEMONIC = -1;

	private String id;

	private String icon;

	private String name;

	private int mnemonic;

	private StringProperty mnemonicName;

	private String type;

	private String shortcut;

	private Stack<ProgramActionHandler> actionStack;

	public ActionProxy() {
		mnemonic = NO_MNEMONIC;
		mnemonicName = new SimpleStringProperty();
		actionStack = new Stack<>();
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon( String icon ) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
		updateMnemonicName();
	}

	public String getMnemonicName() {
		return mnemonicName.get();
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

	public StringProperty getMnemonicNameValue() {
		return mnemonicName;
	}

	public void pushAction( ProgramActionHandler action ) {
		pullAction( action );
		actionStack.push( action );
	}

	public void pullAction( ProgramActionHandler action ) {
		actionStack.remove( action );
	}

	@Override
	public void handle( T event ) {
		if( actionStack.size() == 0 ) return;
		actionStack.peek().handle( event );
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
