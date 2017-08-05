package com.xeomar.xenon.tool;

import com.xeomar.xenon.ProductTool;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.product.ProductMetadata;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.util.Version;
import com.xeomar.xenon.worktool.ToolException;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AboutTool extends ProductTool {

	private static final Logger log = LoggerFactory.getLogger( AboutTool.class );

	private String titleSuffix;

	private Map<String, Node> nodes;

	private StackPane stack;

	private ProductMetadata metadata;

	private BorderPane summaryPane;

	private TextArea summaryText;

	private BorderPane productsPane;

	private TextArea productsText;

	private BorderPane detailsPane;

	private TextArea detailsText;

	public AboutTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-about" );

		setTitleSuffix( product.getResourceBundle().getString( "tool", "about-suffix" ) );

		// NEXT Add the different panes to the stack
		stack = new StackPane();

		summaryText = new TextArea();
		summaryText.setEditable( false );
		//		summaryPane = new BorderPane();
		//		summaryPane.setCenter( summaryText );

		productsText = new TextArea();
		productsText.setEditable( false );
		//		productsPane = new BorderPane();
		//		productsPane.setCenter( productsText );

		detailsText = new TextArea();
		detailsText.setEditable( false );
		//		detailsPane = new BorderPane();
		//		detailsPane.setCenter( detailsText );

		//		stack.getChildren().add( summaryPane );

		nodes = new ConcurrentHashMap<>();
		nodes.put( "summary", summaryText );
		nodes.put( "products", productsText );
		nodes.put( "details", detailsText );

		getChildren().add( summaryText );
	}

	public Set<String> getResourceDependencies() {
		Set<String> resources = new HashSet<>();
		resources.add( ProgramGuideType.URI );
		return resources;
	}

	public String getTitleSuffix() {
		return titleSuffix;
	}

	public void setTitleSuffix( String titleSuffix ) {
		this.titleSuffix = titleSuffix;
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
		Guide<GuideNode> guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.setActive( true );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.info( "Tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.info( "Tool conceal" );
		Guide<GuideNode> guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.setActive( false );
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

	@Override
	protected void resourceRefreshed() {
		ProductMetadata metadata = getResource().getModel();
		if( titleSuffix == null ) {
			setTitle( metadata.getName() );
		} else {
			setTitle( metadata.getName() + " - " + titleSuffix );
		}
		summaryText.setText( getSummaryText( metadata ) );
		productsText.setText( getProductsText( (Program)getProduct() ) );
		detailsText.setText( getDetailsText( (Program)getProduct() ) );
	}

	private String getSummaryText( ProductMetadata metadata ) {
		Version version = new Version( metadata.getVersion() );

		StringBuilder builder = new StringBuilder();
		builder.append( metadata.getName() );
		builder.append( " " );
		builder.append( version.toHumanString() );
		builder.append( "\n" );
		builder.append( "  on " );
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

		return builder.toString();
	}

	private String getProductsText( Program program ) {
		return "Products Information";
	}

	private String getDetailsText( Program program ) {
		return "Program Details";
	}

	private void selectedPage( TreeItem<GuideNode> item ) {
		if( item == null ) return;
		getChildren().clear();
		getChildren().add( nodes.get( item.getValue().getId() ) );
	}

}
