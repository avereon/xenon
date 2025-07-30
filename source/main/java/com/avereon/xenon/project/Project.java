package com.avereon.xenon.project;

import javafx.scene.paint.Paint;

/**
 * Projects are an organizational unit in Xenon. Projects are used to group
 * related things together. For example, a project may contain a set of files,
 * folders, and other projects. Projects are typically used to organize a
 * workspace and provide a way to manage related items.
 */
public interface Project {

	/**
	 * Get the project name.
	 *
	 * @return The project name.
	 */
	String getName();

	/**
	 * Get the project icon.
	 *
	 * @return The project icon.
	 */
	String getIcon();

	/**
	 * Get the project paint.
	 *
	 * @return The project paint.
	 */
	Paint getPaint();

}
