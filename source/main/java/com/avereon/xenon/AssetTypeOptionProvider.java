package com.avereon.xenon;

import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.compare.AssetTypeNameComparator;
import com.avereon.xenon.tool.settings.SettingOptionProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssetTypeOptionProvider implements SettingOptionProvider {

	private final Xenon program;

	public AssetTypeOptionProvider( Xenon program ) {
		this.program = program;
	}

	@Override
	public List<String> getKeys() {
		return program.getResourceManager().getAssetTypes().stream().filter( ResourceType::isUserType ).sorted( new AssetTypeNameComparator() ).map( ResourceType::getKey ).toList();
	}

	@Override
	public String getName( String key ) {
		return getAssetTypeMap().get( key ).getName();
	}

	private Map<String, ResourceType> getAssetTypeMap() {
		return program.getResourceManager().getAssetTypes().stream().collect( Collectors.toMap( ResourceType::getKey, t -> t ) );
	}

}
