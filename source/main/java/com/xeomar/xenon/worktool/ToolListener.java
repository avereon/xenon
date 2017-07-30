package com.xeomar.xenon.worktool;

public interface ToolListener {

	void toolClosing( ToolEvent event ) throws ToolVetoException;

	void toolClosed( ToolEvent event );

}
