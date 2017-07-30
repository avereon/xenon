package com.xeomar.xenon.worktool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ToolInfo {

	private Set<Class<? extends Tool>> requiredToolClasses;

	public Set<Class<? extends Tool>> getRequiredToolClasses() {
		return Collections.unmodifiableSet( requiredToolClasses == null ? new HashSet<>() : requiredToolClasses );
	}

	public void addRequiredToolClass( Class<? extends Tool> toolClass ) {
		if( requiredToolClasses == null ) requiredToolClasses = new CopyOnWriteArraySet<>();
		requiredToolClasses.add( toolClass );
	}

	public void removeRequiredToolClass( Class<? extends Tool> toolClass ) {
		requiredToolClasses.remove( toolClass );
		if( requiredToolClasses.size() == 0 ) requiredToolClasses = null;
	}

}
