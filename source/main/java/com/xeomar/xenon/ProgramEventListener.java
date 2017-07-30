package com.xeomar.xenon;

import java.util.EventListener;

public interface ProgramEventListener extends EventListener {

	void eventOccurred(ProgramEvent event);

}
