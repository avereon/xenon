package com.avereon.xenon.project;

import javafx.scene.paint.Paint;

/**
 * Projects are an organizational unit in Xenon. Projects are used to group
 * related things together. For example, a project may contain a set of files,
 * folders, and other projects. Projects are typically used to organize a
 * workspace and provide a way to manage related items.
 * <p>
 * The initial idea behind a project was how to identify a local project to
 * Xenon with a file, a folder, or some other way. That way the concept of a
 * project to the user can be familiar in Xenon and "definable" outside of
 * Xenon, at least locally? Here are some example ideas:
 * <p>
 *   <ul>
 * 	 <li>The presence of a .xen file in a folder makes the containing folder a project</li>
 *   <li>The presence of a .xen folder in a folder makes the containing folder a project</li>
 *   </ul>
 * <p>
 * Project metadata would need to be stored somewhere, so having non-folder-based
 * projects would need a way to be defined if we choose to support them.
 *
 * <h2>Project Subclasses</h2>
 * What about modules that want to define their own project types? Take Cartesia
 * for example. Cartesia could have a project type that is a collection of
 * design models of various types. Or even include documentation files as well.
 * Can we support the concept of DesignProject that extends the Project class?
 * How can that be helpful to module implementors and users?
 * <ul>
 *   <li>Improved default names and icons could be helpful</li>
 *   <li>Restrictive or filtered resources</li>
 * </ul>
 * This concept will run into the same challenges we have with Resource types,
 * or we need to go the same route as we did with modules.
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

	/**
	 * Get the project description.
	 */
	String getDescription();

	/*
	 * Get the project resource root
	 */
	// TODO Should we build a Tree interface for this?
	//Tree<Resource> getResourceRoot();
	//NodeTree<Resource> getResourceRoot();
	//TreeModel<Resource> getResourceRoot();

}
