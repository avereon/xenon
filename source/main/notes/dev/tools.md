# Developing and Registering Tools

1. Create a [asset type](assets.md) if needed
1. Register the [asset type](assets.md) with the program using the asset manager
	1. program.getAssetManager().registerAssetType( AssetType )
1. Create a tool class
1. Register the tool with a asset type using the tool manager
	1. Create the tool metadata object
	1. program.getToolManager().registerTool( assetType, toolRegistration )

