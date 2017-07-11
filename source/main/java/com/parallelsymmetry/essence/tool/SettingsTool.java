package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.ProgramTool;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.worktool.ToolInfo;

public class SettingsTool extends ProgramTool {

	private static ToolInfo toolInfo = new ToolInfo();

	static {
		getToolInfo().addRequiredToolClass( GuideTool.class );
	}

	public SettingsTool( Program program, Resource resource ) {
		super( program, resource );
	}

	public static ToolInfo getToolInfo() {
		return toolInfo;
	}

}
