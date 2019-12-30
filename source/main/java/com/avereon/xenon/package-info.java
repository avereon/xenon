/**
 * The base package for the program.
 * <h1>Events</h1>
 * The program provides and utilizes a powerful event infrastructure. The main
 * program, most services, assets, mods all use events to provide information
 * to other parts of the program when tight coupling is not intended or desired.
 * JavaFX also has a similar event infrastructure which allows the program to
 * wrap JavaFX events and publish them on the program event infrastructure.
 * <h2>Event Scopes</h2>
 * The program provides several event scopes, implemented by
 * {@link com.avereon.event.EventBus} instances. Most scopes also forward their
 * events to a parent scope, with the program scope being the top most scope.
 * <ul>
 *   <li>Program - {@link com.avereon.xenon.ProgramEvent ProgramEvent}
 *   	 <ul>
 *   	   <li>Task Manager - {@link com.avereon.xenon.task.TaskThreadEvent TaskThreadEvent}
 *   	     <ul>
 *   	       <li>Task - {@link com.avereon.xenon.task.TaskEvent TaskEvent}</li>
 *   	     </ul>
 *   	   <li>Asset Manager
 *   	     <ul>
 *   	       <li>Asset - {@link com.avereon.xenon.asset.AssetEvent AssetEvent}</li>
 *   	     </ul>
 *   	   </li>
 *   	   <li>Product Manager - {@link com.avereon.xenon.product.ModEvent ModEvent}</li>
 *   	   <li>Settings Manager
 *   	     <ul>
 *   	       <li>Settings - {@link com.avereon.settings.SettingsEvent SettingsEvent}</li>
 *   	     </ul>
 *   	   </li>
 *   	 </ul>
 *   </li>
 * </ul>
 */
package com.avereon.xenon;