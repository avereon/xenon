package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.Resource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Worktool class is a control that "works on" a resource.
 */
public abstract class Worktool extends Control {

	private static final Logger log = LoggerFactory.getLogger( Worktool.class );

	private ObjectProperty<Node> graphicProperty;

	private StringProperty titleProperty;

	private Resource resource;

	private Workpane.View parent;

	private boolean allocated;

	private boolean displayed;

	public Worktool( Resource resource ) {
		this.titleProperty = new SimpleStringProperty();
		this.resource = resource;
	}

	public Node getGraphic() {
		return graphicProperty.getValue();
	}

	public void setGraphic( Node graphic ) {
		graphicProperty.setValue( graphic );
	}

	public ObjectProperty graphicProperty() {
		return graphicProperty;
	}

	public void setTitle( String title ) {
		titleProperty.setValue( title );
	}

	public String getTitle() {
		return titleProperty.getValue();
	}

	public StringProperty titleProperty() {
		return titleProperty;
	}

	public Resource getResource() {
		return resource;
	}

	public Workpane.View getToolView() {
		return parent;
	}

	public void setToolView( Workpane.View parent ) {
		this.parent = parent;
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public boolean isAllocated() {
		return allocated;
	}

	public boolean isActive() {
		return parent.getActiveTool() == this;
	}

	/**
	 * Allocate the tool.
	 */
	protected void allocate() throws ToolException {}

	/**
	 * Display the tool.
	 */
	protected void display() throws ToolException {}

	/**
	 * Activate the tool.
	 */
	protected void activate() throws ToolException {}

	/**
	 * Deactivate the tool.
	 */
	protected void deactivate() throws ToolException {}

	/**
	 * Conceal the tool.
	 */
	protected void conceal() throws ToolException {}

	/**
	 * Deallocate the tool.
	 */
	protected void deallocate() throws ToolException {}

	/**
	 * Allocate the tool.
	 */
	final void callAllocate() {
		try {
			allocate();
			allocated = true;
		} catch( ToolException exception ) {
			log.error( "Error allocating tool", exception );
		}
	}

	/**
	 * Display the tool.
	 */
	final void callDisplay() {
		try {
			display();
			displayed = true;
		} catch( ToolException exception ) {
			log.error( "Error displaying tool", exception );
		}
	}

	/**
	 * Activate the tool.
	 */
	final void callActivate() {
		try {
			activate();
		} catch( ToolException exception ) {
			log.error( "Error activating tool", exception );
		}
	}

	/**
	 * Deactivate the tool. Called when the tool is deactivated either by the tool
	 * parent being deactivated or by a different tool being activated.
	 */
	final void callDeactivate() {
		try {
			deactivate();
		} catch( ToolException exception ) {
			log.error( "Error deactivating tool", exception );
		}
	}

	/**
	 * Conceal the tool. Called when the tool is visible and then is hidden by
	 * another tool or removed from the tool parent.
	 */
	final void callConceal() {
		try {
			conceal();
			displayed = false;
		} catch( ToolException exception ) {
			log.error( "Error concealing tool", exception );
		}
	}

	/**
	 * Deallocate the tool. Called when the tool is removed from the tool parent.
	 */
	final void callDeallocate() {
		try {
			deallocate();
			allocated = false;
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

}
