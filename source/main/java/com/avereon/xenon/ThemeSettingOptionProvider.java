package com.avereon.xenon;

import com.avereon.xenon.tool.settings.SettingOptionProvider;

import java.util.List;
import java.util.stream.Collectors;

public class ThemeSettingOptionProvider implements SettingOptionProvider {

	private Program program;

	public ThemeSettingOptionProvider( Program program ) {
		this.program = program;
	}

	private Program getProgram() {
		return program;
	}

	@Override
	public List<String> getKeys() {
		return getProgram().getThemeManager().getThemes().stream().sorted().map( ThemeMetadata::getId ).collect( Collectors.toList() );
	}

	@Override
	public String getName( String key ) {
		return getProgram().getThemeManager().getMetadata( key ).getName();
	}

	@Override
	public String getValue( String key ) {
		return key;
	}

}
