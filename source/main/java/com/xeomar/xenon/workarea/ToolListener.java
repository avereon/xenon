package com.xeomar.xenon.workarea;

public interface ToolListener {

	void toolClosing( ToolEvent event ) throws ToolVetoException;

	void toolClosed( ToolEvent event );

}
