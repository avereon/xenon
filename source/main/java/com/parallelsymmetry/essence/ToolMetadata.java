package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.worktool.Tool;
import javafx.scene.Node;

public class ToolMetadata {

	private Product product;

	private Class<? extends Tool> type;

	private String name;

	private Node icon;

	public ToolMetadata( Product product, Class<? extends Tool> type, String name, Node icon ) {
		this.product = product;
		this.type = type;
		this.name = name;
		this.icon = icon;
	}

	public Product getProduct() {
		return product;
	}

	public Class<? extends Tool> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Node getIcon() {
		return icon;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		return type.equals( object );
	}

	@Override
	public String toString() {
		return getName();
	}

}
