package com.xeomar.xenon.update;

import java.nio.file.Path;

class InstalledProduct {

	private Path target;

	/*
	 * This constructor is used by the settings API via reflection.
	 */
	@SuppressWarnings( "unused" )
	public InstalledProduct() {}

	public InstalledProduct( Path target ) {
		this.target = target;
	}

	public Path getTarget() {
		return target;
	}

	public void setTarget( Path target ) {
		this.target = target;
	}

	@Override
	public String toString() {
		return target.toString();
	}

	@Override
	public int hashCode() {
		return target.toString().hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		return object instanceof InstalledProduct && this.toString().equals( object.toString() );
	}

}
