package com.avereon.xenon;

import com.avereon.xenon.util.ActionUtil;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCombination;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Associated with menu items and tool bar buttons as a proxy for that item so
 * more than one action can be pushed and pulled from the proxy without loosing
 * what was already registered.
 */
public class ActionProxy implements EventHandler<ActionEvent> {

	public static final int NO_MNEMONIC = -1;

	private String id;

	private final StringProperty icon;

	private String name;

	private int mnemonic;

	private final StringProperty mnemonicName;

	private final StringProperty description;

	private String type;

	private String shortcut;

	private KeyCombination accelerator;

	private String command;

	private final List<String> states;

	private final Map<String, ActionState> stateMap;

	private String currentState;

	private final Stack<Action> actionStack;

	private final BooleanProperty enabledProperty;

	public ActionProxy() {
		mnemonic = NO_MNEMONIC;
		icon = new SimpleStringProperty();
		mnemonicName = new SimpleStringProperty();
		description = new SimpleStringProperty();
		enabledProperty = new SimpleBooleanProperty();
		states = new CopyOnWriteArrayList<>();
		stateMap = new ConcurrentHashMap<>();
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

	@SuppressWarnings( "unused" )
	public int getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic( int mnemonic ) {
		this.mnemonic = mnemonic;
		updateMnemonicName();
	}

	void setMnemonic( String mnemonic ) {
		try {
			setMnemonic( Integer.parseInt( mnemonic ) );
		} catch( NumberFormatException exception ) {
			setMnemonic( ActionProxy.NO_MNEMONIC );
		}
	}

	@SuppressWarnings( "unused" )
	public ReadOnlyStringProperty descriptionProperty() {
		return description;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription( String description ) {
		this.description.set( description );
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
		this.accelerator = ActionUtil.parseShortcut( shortcut );
	}

	public KeyCombination getAccelerator() {
		return accelerator;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand( String command ) {
		this.command = command;
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

	public void addState( String id, String name, String icon ) {
		stateMap.put( id, new ActionState( id, name, icon ) );
		states.add( id );
		if( states.size() == 1 ) setState( id );
	}

	@SuppressWarnings( "unused" )
	public List<String> getStates() {
		return Collections.unmodifiableList( states );
	}

	@SuppressWarnings( "unused" )
	public String getStateName( String id ) {
		return stateMap.get( id ).getName();
	}

	public String getStateIcon( String id ) {
		return Optional.ofNullable( stateMap.get( id ) ).map( ActionState::getIcon ).orElse( null );
	}

	public String getStateAfter( String state ) {
		if( states.isEmpty() ) return null;
		int index = states.indexOf( state ) + 1;
		if( index >= states.size() ) index = 0;
		return states.get( index );
	}

	public String getNextState() {
		return getStateAfter( currentState );
	}

	public void setState( String state ) {
		if( state == null ) return;
		setIcon( getStateIcon( state ) );
		currentState = state;
	}

	public String getState() {
		return currentState;
	}

	@Override
	public void handle( ActionEvent event ) {
		if( event.isConsumed() || actionStack.size() == 0 || !isEnabled() ) return;
		actionStack.peek().handle( event );
		setState( getNextState() );
		event.consume();
	}

	public void pushAction( Action action ) {
		if( action == null ) return;
		pullAction( action );
		actionStack.push( action );
		action.setActionProxy( this );
		updateEnabled();
	}

	public void pullAction( Action action ) {
		if( action == null ) return;
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

	private static class ActionState {

		private final String id;

		private final String name;

		private final String icon;

		private ActionState( String id, String name, String icon ) {
			this.id = id;
			this.name = name;
			this.icon = icon;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getIcon() {
			return icon;
		}

	}

}
