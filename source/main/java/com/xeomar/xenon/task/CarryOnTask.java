package com.xeomar.xenon.task;

public abstract class CarryOnTask<R, B> extends Task<R> {

	private B carryOn;

	public CarryOnTask() {
		super();
	}

	public CarryOnTask( String name ) {
		super( name );
	}

	public CarryOnTask( String name, Priority priority ) {
		super( name, priority );
	}

	public B getCarryOn() {
		return carryOn;
	}

	public void setCarryOn( B carryOn ) {
		this.carryOn = carryOn;
	}

}
