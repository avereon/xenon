package com.parallelsymmetry.essence;

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

	private String id;

	private IconRenderer icon;

	private String name;

	private String mnemonic;

	private String shortcut;

	private Stack<ProgramAction> actionStack;

	public ActionProxy() {}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public IconRenderer getIcon() {
		return icon;
	}

	public void setIcon( IconRenderer icon ) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic( String mnemonic ) {
		this.mnemonic = mnemonic;
	}

	public String getShortcut() {
		return shortcut;
	}


	public void setShortcut( String shortcut ) {
		this.shortcut = shortcut;
	}

	public void pushAction( ProgramAction action ) {
		pullAction( action );
		actionStack.push(action);
	}

	public void pullAction( ProgramAction action ) {
		actionStack.remove( action );
	}

	@Override
	public void handle( T event ) {
		// TODO Get the head of the stack and execute
		ProgramAction action = actionStack.peek();
		action.handle( event );
	}
}
