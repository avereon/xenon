package com.avereon.xenon;

import com.avereon.xenon.workpane.Workpane;
import javafx.scene.Node;

public class ToolRegistration {

	private XenonProgramProduct product;

	private Class<? extends ProgramTool> type;

	private String name;

	private Node icon;

	private Workpane.Placement placement;

	private ToolInstanceMode instanceMode;

	public ToolRegistration( XenonProgramProduct product, Class<? extends ProgramTool> type ) {
		this.product = product;
		this.type = type;
	}

	public XenonProgramProduct getProduct() {
		return product;
	}

	public ToolRegistration setProduct( XenonProgramProduct product ) {
		this.product = product;
		return this;
	}

	public Class<? extends ProgramTool> getType() {
		return type;
	}

	public ToolRegistration setType( Class<? extends ProgramTool> type ) {
		this.type = type;
		return this;
	}

	public String getName() {
		return name;
	}

	public ToolRegistration setName( String name ) {
		this.name = name;
		return this;
	}

	public Node getIcon() {
		return icon;
	}

	public ToolRegistration setIcon( Node icon ) {
		this.icon = icon;
		return this;
	}

	public Workpane.Placement getPlacement() {
		return placement;
	}

	public ToolRegistration setPlacement( Workpane.Placement placement ) {
		this.placement = placement;
		return this;
	}

	public ToolInstanceMode getInstanceMode() {
		return instanceMode;
	}

	public ToolRegistration setInstanceMode( ToolInstanceMode instanceMode ) {
		this.instanceMode = instanceMode;
		return this;
	}

	@Override
	public boolean equals( Object object ) {
		return type.equals( object );
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

}
