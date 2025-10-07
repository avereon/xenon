package com.avereon.xenon.tool;

import com.avereon.util.FileUtil;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zerra.color.Paints;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebView;
import lombok.CustomLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@CustomLog
public class HelpTool extends GuidedTool {

	//	private final MarkdownView mdfx;

	private final WebView web;

	public HelpTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "help" ) );

		web = new WebView();
		web.setPageFill( Color.TRANSPARENT );

		Label label = new Label( "" );
		label.textFillProperty().addListener( ( p, o, n ) -> updateTextFill( n ) );

		getChildren().addAll( label, web );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		// The asset type should load the help content
		// The asset model should be a Markdown document
		String content = request.getResource().getModel();
		//log.atConfig().log( "content=" + content );
		//content = content == null ? "null" : content;
		//mdfx.setMdString( content );

		web.getEngine().titleProperty().addListener( ( p, o, n ) -> setTitle( n ) );
		web.getEngine().loadContent( content );
	}

	private void updateTextFill( Paint paint ) {
		Path path = getProgram().getDataFolder().resolve( "settings" ).resolve( "ui" ).resolve( "browser.css" );
		String style = "body {color:" + Paints.toString( paint ) + ";background:transparent;font-family:sans-serif;}";
		try {
			FileUtil.save( style, path, StandardCharsets.UTF_8 );
			web.getEngine().setUserStyleSheetLocation( path.toUri().toString() );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

}
