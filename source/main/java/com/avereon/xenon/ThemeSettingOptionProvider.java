package com.avereon.xenon;

import com.avereon.xenon.tool.settings.SettingOptionProvider;

import java.util.Set;
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
	public Set<String> getKeys() {
		return getProgram().getThemeManager().getThemes().stream().map( ThemeMetadata::getId ).collect( Collectors.toSet() );
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
