package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.worktool.ToolException;
import com.parallelsymmetry.essence.worktool.ToolInfo;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class AboutTool extends ProductTool {

	private static final Logger log = LoggerFactory.getLogger( AboutTool.class );

	private static ToolInfo toolInfo = new ToolInfo();

	private String titleSuffix;

	private StackPane stack;

	private ProductMetadata metadata;

	private TextArea text;

	static {
		getToolInfo().addRequiredToolClass( GuideTool.class );
	}

	public AboutTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-about" );

		setTitleSuffix( product.getResourceBundle().getString( "tool", "about-suffix" ) );

		// NEXT Add the different panes to the stack
		stack = new StackPane();

		text = new TextArea();
		text.setEditable( false );

		stack.getChildren().add(text );

		getChildren().add( stack );
	}

	public String getTitleSuffix() {
		return titleSuffix;
	}

	public void setTitleSuffix( String titleSuffix ) {
		this.titleSuffix = titleSuffix;
	}

	public ProductMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata( ProductMetadata metadata ) {
		this.metadata = metadata;
		if( titleSuffix == null ) {
			setTitle( metadata.getName() );
		} else {
			setTitle( metadata.getName() + " - " + titleSuffix );
		}

		StringBuilder builder = new StringBuilder();
		builder.append( metadata.getName() );
		builder.append( " " );
		builder.append( metadata.getVersion() );
		builder.append( "\n" );
		builder.append( "  build " );
		builder.append( metadata.getTimestamp() );
		builder.append( " UTC\n" );
		builder.append( "  by " );
		builder.append( metadata.getProvider() );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "java.vm.name" ) );
		builder.append( " (" );
		builder.append( System.getProperty( "java.vm.version" ) );
		builder.append( ", " );
		builder.append( System.getProperty( "java.vm.info" ) );
		builder.append( ")\n" );
		builder.append( "  by " );
		builder.append( System.getProperty( "java.vm.vendor" ) );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "os.name" ) );
		builder.append( " " );
		builder.append( System.getProperty( "os.version" ) );
		builder.append( " " );
		builder.append( System.getProperty( "os.arch" ) );
		builder.append( "\n" );

		builder.append( "\n" );
		builder.append( "\n" );

		int year = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ).get( Calendar.YEAR );

		builder.append( "\u00A9" );
		if( year == metadata.getInception() ) {
			builder.append( metadata.getInception() );
		} else {
			builder.append( metadata.getInception() );
			builder.append( "-" );
			builder.append( year );
		}
		builder.append( " " );
		builder.append( metadata.getProvider() );
		builder.append( " " );
		builder.append( metadata.getCopyrightSummary() );
		builder.append( "\n" );

		text.setText( builder.toString().trim() );
	}

	@Override
	protected void allocate() throws ToolException {
		log.info( "Tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.info( "Tool display" );
	}

	@Override
	protected void activate() throws ToolException {
		log.info( "Tool activate" );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.info( "Tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.info( "Tool conceal" );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.info( "Tool deallocate" );
	}

	@Override
	protected void resourceReady() throws ToolException {
		log.info( "Resource ready" );

		Guide<GuideNode> guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.selectedItemProperty().addListener( ( obs, oldSelection, newSelection ) -> {
			selectedPage( newSelection );
		} );

		resourceRefreshed();
	}

	private void selectedPage( TreeItem<GuideNode> item ) {
		GuideNode node = item.getValue();

		// The guide nodes need to be loosely connected to the tool(s)

		System.out.println( "Set page: " + node.getId() );

		switch( node.getId() ) {
			case "summary" : {
				break;
			}
			case "products" : {
				break;
			}
			case "details" : {
				break;
			}
		}
	}

	@Override
	protected void resourceRefreshed() {
		setMetadata( getResource().getModel() );
	}

	public Set<Class<? extends ProductTool>> getToolDependencies() {
		Set<Class<? extends ProductTool>> tools = new HashSet<>();
		tools.add( GuideTool.class );
		return tools;
	}

	public static ToolInfo getToolInfo() {
		return toolInfo;
	}

}
