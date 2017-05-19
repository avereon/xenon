package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.Resource;
import javafx.scene.control.Control;

/**
 * The Worktool class is a control that "works on" a resource.
 */
public abstract class Worktool extends Control {

	private Resource resource;

	public Worktool( Resource resource ) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

}
