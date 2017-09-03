package com.xeomar.xenon;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.product.ProductMetadata;

import java.io.File;

public abstract class Module implements Product, Comparable<Module> {

	protected Program program;

	protected ProductMetadata card;

	public Module( Program program, ProductMetadata moduleCard ) {
		this.program = program;
		this.card = moduleCard;
	}

	public Program getProgram() {
		return program;
	}

	/**
	 * Get the product card.
	 *
	 * @return
	 */
	@Override
	public ProductMetadata getMetadata() {
		return card;
	}

	/**
	 * Called by the program to register a module instance. This method is called
	 * before the program frame and workspaces are available.
	 */
	public abstract void register();

	/**
	 * Called by the program to create a module instance. This method is called
	 * after the program frame and workspaces are available.
	 */
	public abstract void create();

	/**
	 * Called by the program to destroy a module instance. This method is called
	 * before the program frame and workspaces are unavailable.
	 */
	public abstract void destroy();

	/**
	 * Called by the program to unregister a module instance. This method is
	 * called after the program frame and workspaces are unavailable.
	 */
	public abstract void unregister();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getDataFolder() {
		return new File( program.getDataFolder(), card.getProductKey() );
	}

	/**
	 * This implementation only compares the product card artifact values.
	 */
	@Override
	public int compareTo( Module that ) {
		return this.card.getArtifact().compareTo( that.card.getArtifact() );
	}

	/**
	 * This implementation only returns the product card name.
	 */
	@Override
	public String toString() {
		return card.getName();
	}

}
