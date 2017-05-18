package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.Resource;
import javafx.scene.control.Control;

/**
 * The WorkTool class is a control that "works on" a resource.
 */
public abstract class WorkTool extends Control {

	private Resource resource;

	public WorkTool( Resource resource ) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

}
