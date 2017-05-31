package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;

import java.io.File;

public abstract class Module implements Product, Comparable<Module> {

	protected Program service;

	protected ProductMetadata card;

	public Module( Program service, ProductMetadata moduleCard ) {
		this.service = service;
		this.card = moduleCard;
	}

	public Program getProgram() {
		return service;
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
		return new File( service.getDataFolder(), card.getProductKey() );
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
