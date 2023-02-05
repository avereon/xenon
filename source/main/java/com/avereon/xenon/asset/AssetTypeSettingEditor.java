package com.avereon.xenon.asset;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AssetTypeSettingEditor extends SettingEditor {

	private final Label label = new Label( "Asset Type Setting Editor" );

	private final Label key;

	private final Label name;

	private final Label description;

	public AssetTypeSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );

		key = new Label();
		name = new Label();
		description = new Label();

		doUpdateFields( setting.getSettings().get( getKey(), String.class, "" ) );
	}

	private void doUpdateFields( String typeKey ) {
		AssetType type = getProduct().getProgram().getAssetManager().getAssetType( typeKey );
		key.setText( type == null ? "" : type.getKey() );
		name.setText( type == null ? "" : type.getName() );
		description.setText( type == null ? "" : type.getDescription() );

		if( type == null ) return;

		// What about tool associations?
		List<Class<? extends ProgramTool>> toolClasses = getProduct().getProgram().getToolManager().getRegisteredTools( type );
		Class<? extends ProgramTool> toolClass = getProduct().getProgram().getToolManager().getDefaultTool( type );

		type.getCodecs();
		Codec defaultCodec = type.getDefaultCodec();
		type.getIcon();

		type.getCodecs().forEach( c -> {
			System.out.printf( "%s%n", c.getName() );
			Arrays.stream( Codec.Pattern.values() ).forEach( p -> {
				System.out.printf( "  %s%n", p );
				Set<String> supported = c.getSupported( p );
				supported.forEach( s -> {
					System.out.printf( "    %s%n", s );
				} );
			} );
		} );

		toolClasses.forEach( c -> {
			boolean d  = toolClass == c;
			System.out.printf( "%s %s%n", c.getName(), d ? "*" : "" );
		} );

		//defaultCodec.getSupported( Codec.Pattern.EXTENSION );

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addComponents( GridPane pane, int row ) {
		GridPane grid = new GridPane();

		int index = 0;
		grid.addRow( index++, key );
		grid.addRow( index++, name );
		grid.addRow( index++, description );

		GridPane.setColumnSpan( grid, GridPane.REMAINING );

		pane.addRow( row, grid );
	}

	@Override
	protected Set<Node> getComponents() {
		return Set.of( label, key, name );
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		doUpdateFields( String.valueOf( event.getNewValue() ) );
	}
}
