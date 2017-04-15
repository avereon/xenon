package com.parallelsymmetry.essence;

import java.util.EventListener;

public interface ProgramEventListener extends EventListener {

	void eventOccurred(ProgramEvent event);

}
