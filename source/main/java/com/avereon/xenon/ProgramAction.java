package com.avereon.xenon;

import com.avereon.zarra.javafx.Fx;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.CustomLog;

@CustomLog
public abstract class ProgramAction implements EventHandler<ActionEvent> {

	public static final String NAME_SUFFIX = ".name";

	public static final String ICON_SUFFIX = ".icon";

	public static final String TYPE_SUFFIX = ".type";

	public static final String COMMAND_SUFFIX = ".command";

	public static final String MNEMONIC_SUFFIX = ".mnemonic";

	public static final String SHORTCUT_SUFFIX = ".shortcut";

	public static final String DESCRIPTION_SUFFIX = ".description";

	private static final ActionProxy NONE = new ActionProxy();

	private final Program program;

	private ActionProxy proxy = NONE;

	protected ProgramAction( Program program ) {
		if( program == null ) throw new NullPointerException( "Program cannot be null" );
		this.program = program;
	}

	public final Program getProgram() {
		return program;
	}

	public abstract void handle( ActionEvent event );

	/**
	 * Override this method with the logic to determine if the action is enabled.
	 *
	 * @return If the action is enabled
	 */
	public boolean isEnabled() {
		return false;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	public ProgramAction updateEnabled() {
		if( proxy != null ) proxy.setEnabled( isEnabled() );
		return this;
	}

	/**
	 * Get the current state of the action for multi-state actions.
	 *
	 * @return The state id
	 */
	public String getState() {
		return proxy.getState();
	}

	/**
	 * Set the action state for multi-state actions.
	 *
	 * @param id The state id
	 */
	public void setState( String id ) {
		Fx.run( () -> proxy.setState( id ) );
	}

	void setActionProxy( ActionProxy proxy ) {
		this.proxy = proxy == null ? NONE : proxy;
	}

}
