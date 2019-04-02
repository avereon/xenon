package com.xeomar.xenon.mod;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;

import java.nio.file.Path;

/**
 * The super class for modules.
 */
public abstract class Mod implements ProgramProduct, Comparable<Mod> {

	private Program program;

	private ProductCard card;

	public Mod() throws Exception {
		card = new ProductCard().load( this );
	}

	@Override
	public Program getProgram() {
		return program;
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	public final void init( Program program, ProductCard card ) {
		this.program = program;
		this.card = card;
	}

	/**
	 * Called by the program to register a module instance. This method is called before the program frame and workspaces are available.
	 */
	public void register() {}

	/**
	 * Called by the program to create a module instance. This method is called after the program frame and workspaces are available.
	 */
	public void create() {}

	/**
	 * Called by the program to destroy a module instance. This method is called before the program frame and workspaces are unavailable.
	 */
	public void destroy() {}

	/**
	 * Called by the program to unregister a module instance. This method is called after the program frame and workspaces are unavailable.
	 */
	public void unregister() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getDataFolder() {
		return program.getDataFolder().resolve( card.getProductKey() );
	}

	/**
	 * This implementation only compares the product card artifact values.
	 */
	@Override
	public int compareTo( Mod that ) {
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
