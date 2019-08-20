package com.avereon.xenon.tool.about;

import com.avereon.product.ProductCard;
import com.avereon.settings.Settings;
import com.avereon.util.*;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workarea.ToolException;
import com.avereon.xenon.workarea.ToolParameters;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.tbee.javafx.scene.layout.MigPane;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AboutTool extends GuidedTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String SUMMARY = "summary";

	public static final String PRODUCTS = "products";

	public static final String DETAILS = "details";

	private static final double ICON_SIZE = 96;

	private String titleSuffix;

	private Map<String, Node> pages;

	private SummaryPane summaryPane;

	private TextArea summaryText;

	private BorderPane productsPane;

	private TextArea productsText;

	private BorderPane detailsPane;

	private TextArea detailsText;

	public AboutTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-about" );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "about" ) );
		setTitleSuffix( product.getResourceBundle().getString( "tool", "about-suffix" ) );

		summaryPane = new SummaryPane();

		productsText = new TextArea();
		productsText.setEditable( false );
		productsPane = new BorderPane();
		productsPane.setCenter( productsText );

		detailsText = new TextArea();
		detailsText.setEditable( false );
		detailsText.setFont( Font.font( "Monospaced", 12.0 ) );
		detailsPane = new BorderPane();
		detailsPane.setCenter( detailsText );

		pages = new ConcurrentHashMap<>();
		pages.put( SUMMARY, summaryPane );
		pages.put( PRODUCTS, productsPane );
		pages.put( DETAILS, detailsPane );
	}

	public String getTitleSuffix() {
		return titleSuffix;
	}

	public void setTitleSuffix( String titleSuffix ) {
		this.titleSuffix = titleSuffix;
	}

	@Override
	protected void allocate() throws ToolException {
		log.debug( "Tool allocate" );
		super.allocate();
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Tool display" );
		super.display();
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Tool activate" );
		super.activate();
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Tool deallocate" );
		super.deallocate();
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		resourceRefreshed();
		selectPage( SUMMARY );
	}

	@Override
	protected void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
		ProductCard metadata = getResource().getModel();
		if( titleSuffix == null ) {
			setTitle( metadata.getName() );
		} else {
			setTitle( metadata.getName() + " - " + titleSuffix );
		}
		//summaryText.setText( getSummaryText( metadata ) );
		summaryPane.update( metadata );
		productsText.setText( getProductsText( (Program)getProduct() ) );
		detailsText.setText( getDetailsText( (Program)getProduct() ) );
	}

	@Override
	public void setSettings( Settings settings ) {
		super.setSettings( settings );

		Platform.runLater( () -> selectPage( settings.get( GUIDE_SELECTED_IDS, SUMMARY ).split( "," )[ 0 ] ) );
	}

	private class SummaryPane extends MigPane {

		private Label productName;

		private Label productVersion;

		private Label productProvider;

		private Label javaLabel;

		private Label javaName;

		private Label javaVmName;

		private Label javaVersion;

		private Label javaProvider;

		private Label osLabel;

		private Label osName;

		private Label osVersion;

		private Label osProvider;

		private Label informationLabel;

		private String lastUpdateCheckPrompt;

		private Label lastUpdateTimestamp;

		private String nextUpdateCheckPrompt;

		private Label nextUpdateTimestamp;

		public SummaryPane() {
			lastUpdateCheckPrompt = getProduct().getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-last" );
			nextUpdateCheckPrompt = getProduct().getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-next" );

			add( getProgram().getIconLibrary().getIcon( "program", ICON_SIZE ), "spany, aligny top" );
			add( productName = makeLabel( "tool-about-title" ) );
			add( productVersion = makeLabel( "tool-about-version" ), "newline, span 2 1" );
			add( productProvider = makeLabel( "tool-about-provider" ), "newline" );

			add( makeLabel( "tool-about-separator" ), "newline" );
			//add( getProgram().getIconLibrary().getIcon( "java", 64 ), "newline, span 1 2" );
			add( javaLabel = makeLabel( "tool-about-header" ), "newline" );
			//add( javaName = makeLabel( "tool-about-name" ), "newline" );
			add( javaVmName = makeLabel( "tool-about-version" ), "newline" );
			add( javaVersion = makeLabel( "tool-about-version" ), "newline, span 2 1" );
			add( javaProvider = makeLabel( "tool-about-provider" ), "newline" );

			add( makeLabel( "tool-about-separator" ), "newline" );
			//add( getProgram().getIconLibrary().getIcon( osFamily, 64 ), "newline, span 1 2" );
			add( osLabel = makeLabel( "tool-about-header" ), "newline" );
			add( osName = makeLabel( "tool-about-name" ), "newline" );
			add( osVersion = makeLabel( "tool-about-version" ), "newline, span 2 1" );
			add( osProvider = makeLabel( "tool-about-provider" ), "newline" );

			add( makeLabel( "tool-about-separator" ), "newline" );
			add( informationLabel = makeLabel( "tool-about-header" ), "newline" );
			add( lastUpdateTimestamp = makeLabel( "tool-about-version" ), "newline" );
			add( nextUpdateTimestamp = makeLabel( "tool-about-version" ), "newline" );
		}

		public void update( ProductCard card ) {
			String from = getProgram().getResourceBundle().getString( "tool", "about-from" );
			productName.setText( card.getName() );
			if( card.getRelease().getVersion().isSnapshot() ) {
				productVersion.setText( card.getRelease().toHumanString( TimeZone.getDefault() ) );
			} else {
				productVersion.setText( card.getRelease().getVersion().toHumanString() );
			}
			productProvider.setText( from + " " + card.getProvider() );

			javaLabel.setText( "Java" );
			javaVmName.setText( System.getProperty( "java.vm.name" ) );
			String javaVersionDate = System.getProperty( "java.version.date" );
			if( javaVersionDate == null ) {
				javaVersion.setText( System.getProperty( "java.runtime.version" ) );
			} else {
				javaVersion.setText( System.getProperty( "java.runtime.version" ) + "  " + javaVersionDate );
			}
			javaProvider.setText( from + " " + System.getProperty( "java.vm.vendor" ) );

			String osNameString = OperatingSystem.getFamily().toString().toLowerCase();
			osLabel.setText( getProduct().getResourceBundle().getString( "tool", "about-system" ) );
			osName.setText( osNameString.substring( 0, 1 ).toUpperCase() + osNameString.substring( 1 ) );
			osVersion.setText( OperatingSystem.getVersion() );
			osProvider.setText( from + " " + OperatingSystem.getProvider() );

			informationLabel.setText( getProgram().getResourceBundle().getString( BundleKey.LABEL, "information" ) );
			updateUpdateCheckInfo( lastUpdateTimestamp, nextUpdateTimestamp );
		}

		private void updateUpdateCheckInfo( Label lastUpdateCheckField, Label nextUpdateCheckField ) {
			long lastUpdateCheck = getProgram().getProductManager().getLastUpdateCheck();
			long nextUpdateCheck = getProgram().getProductManager().getNextUpdateCheck();
			if( nextUpdateCheck < System.currentTimeMillis() ) nextUpdateCheck = 0;

			String unknown = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "unknown" );
			String notScheduled = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "not-scheduled" );
			String lastUpdateCheckText = lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ),
				DateUtil.DEFAULT_DATE_FORMAT,
				TimeZone.getDefault()
			);
			String nextUpdateCheckText = nextUpdateCheck == 0 ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ),
				DateUtil.DEFAULT_DATE_FORMAT,
				TimeZone.getDefault()
			);

			Platform.runLater( () -> {
				lastUpdateCheckField.setText( lastUpdateCheckPrompt + "  " + lastUpdateCheckText );
				nextUpdateCheckField.setText( nextUpdateCheckPrompt + "  " + nextUpdateCheckText );
			} );
		}

	}

	private Label makeLabel( String labelClass ) {
		Label label = new Label();
		label.getStyleClass().addAll( labelClass );
		return label;
	}

	private String getSummaryText( ProductCard metadata ) {
		StringBuilder builder = new StringBuilder();
		builder.append( metadata.getName() );
		builder.append( " " );
		builder.append( metadata.getRelease().toHumanString( TimeZone.getDefault() ) );
		builder.append( "\n  by " );
		builder.append( metadata.getProvider() );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "java.runtime.name" ) );
		builder.append( " " );
		builder.append( System.getProperty( "java.runtime.version" ) );
		builder.append( "\n  by " );
		builder.append( System.getProperty( "java.vm.vendor" ) );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "os.name" ) );
		builder.append( " " );
		builder.append( System.getProperty( "os.version" ) );
		builder.append( "\n" );

		builder.append( "\n" );
		builder.append( "\n" );

		int year = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ).get( Calendar.YEAR );

		builder.append( "\u00A9" ); // Copyright symbol
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
		StringBuilder builder = new StringBuilder();

		builder.append( "Product Information" );

		return builder.toString();
	}

	private String getDetailsText( Program program ) {
		ProductCard metadata = program.getCard();
		StringBuilder builder = new StringBuilder();

		// Framework summary
		builder.append( getHeader( "Program: " + metadata.getName() + " " + metadata.getVersion() ) );

		// The current date
		builder.append( "\n" );
		builder.append( getHeader( "Timestamp: " + DateUtil.format( new Date(), DateUtil.DEFAULT_DATE_FORMAT ) ) );

		// JVM commands
		builder.append( "\n" );
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		builder.append( getHeader( "JVM commands: " + TextUtil.toString( runtimeMXBean.getInputArguments(), " " ) ) );

		// Program commands
		Application.Parameters parameters = program.getParameters();
		builder.append( "\n" );
		builder.append( getHeader( "Program commands: " + (parameters == null ? "" : TextUtil.toString( parameters.getRaw(), " " )) ) );

		// Program details
		builder.append( "\n" );
		builder.append( getHeader( "Program details" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getProgramDetails( program ), 4, " " ) );

		// Product details
		builder.append( "\n" );
		builder.append( getHeader( "Product details" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getProductDetails( program.getCard() ), 4, " " ) );

		// Runtime
		builder.append( "\n" );
		builder.append( getHeader( "Runtime details" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getRuntimeDetail(), 4, " " ) );

		// Operating system
		builder.append( "\n" );
		builder.append( getHeader( "Operating system" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getOperatingSystemDetail(), 4, " " ) );

		// Execution
		builder.append( "\n" );
		builder.append( getHeader( "Execution details" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getExecutionDetail( program ), 4, " " ) );

		// Memory
		builder.append( "\n" );
		builder.append( getHeader( "Memory details" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getMemoryDetail(), 4, " " ) );

		//		// Installed modules
		//		builder.append( "\n" );
		//		builder.append( getHeader( "Installed modules" ) );
		//
		//		builder.append( "\n" );
		//		Set<ServiceModule> modules = getProgram().getProductManager().getModules();
		//		if( modules.size() == 0 ) {
		//			builder.append( "    No optional modules installed.\n" );
		//		} else {
		//			List<ServiceModule> moduleList = new ArrayList<ServiceModule>( modules );
		//			Collections.sort( moduleList );
		//			for( ServiceModule module : moduleList ) {
		//				builder.append( Indenter.indent( getProductDetails( module.getCard() ), 4, " " ) );
		//				builder.append( "\n" );
		//			}
		//		}

		// Threads
		builder.append( "\n" );
		builder.append( getHeader( "Threads" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getThreadDetail(), 4, " " ) );

		// System properties
		builder.append( "\n" );
		builder.append( getHeader( "System properties" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getProperties( System.getProperties() ), 4, " " ) );

		//		// Settings
		//		builder.append( "\n" );
		//		builder.append( getHeader( "Settings" ) );
		//		builder.append( "\n" );
		//		builder.append( Indenter.indent( getSettingsDetail(), 4, " " ) );

		return builder.toString();
	}

	private String getHeader( String text ) {
		StringBuilder builder = new StringBuilder();

		builder.append( "--- " );
		builder.append( text );
		builder.append( "\n" );

		return builder.toString();
	}

	private String getProgramDetails( Program program ) {
		StringBuilder builder = new StringBuilder();

		builder.append( "Home folder: " + program.getHomeFolder() ).append( "\n" );
		builder.append( "Data folder: " + program.getDataFolder() ).append( "\n" );
		builder.append( "Log file:    " + LogUtil.getLogFile() ).append( "\n" );

		return builder.toString();
	}

	private String getProductDetails( ProductCard card ) {
		StringBuilder builder = new StringBuilder();

		builder.append( "Product:     " + card.getName() ).append( "\n" );
		builder.append( "Provider:    " + card.getProvider() ).append( "\n" );
		builder.append( "Inception:   " + card.getInception() ).append( "\n" );
		builder.append( "Summary:     " + card.getSummary() ).append( "\n" );

		builder.append( "Group:       " + card.getGroup() ).append( "\n" );
		builder.append( "Artifact:    " + card.getArtifact() ).append( "\n" );
		builder.append( "Version:     " + card.getVersion() ).append( "\n" );
		builder.append( "Timestamp:   " + card.getTimestamp() ).append( "\n" );
		builder.append( "Source URI:  " + card.getProductUri() ).append( "\n" );

		//		ProductManager productManager = getProgram().getProductManager();
		//		builder.append( "Enabled:     " + productManager.isEnabled( card )).append( "\n" );
		//		builder.append( "Updatable:   " + productManager.isUpdatable( card )).append( "\n" );
		//		builder.append( "Removable:   " + productManager.isRemovable( card )).append( "\n" );

		return builder.toString();
	}

	private String getRuntimeDetail() {
		StringBuilder builder = new StringBuilder();

		long max = Runtime.getRuntime().maxMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - Runtime.getRuntime().freeMemory();
		builder
			.append( "JVM Memory:     " + FileUtil.getHumanSizeBase2( used ) + " / " + FileUtil.getHumanSizeBase2( total ) + " / " + FileUtil.getHumanSizeBase2( max ) )
			.append( "\n" );

		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
		builder.append( "CPU Cores:      " + bean.getAvailableProcessors() ).append( "\n" );

		boolean scene3d = Platform.isSupported( ConditionalFeature.SCENE3D );
		builder.append( "3D Accelerated: " + scene3d ).append( "\n" );

		builder.append( "\n" );
		Screen primary = Screen.getPrimary();
		Screen.getScreens().forEach( ( screen ) -> {
			builder.append( getScreenDetail( primary, screen ) );
		} );

		return builder.toString();

	}

	private String getScreenDetail( Screen primary, Screen screen ) {
		boolean isPrimary = primary.hashCode() == screen.hashCode();
		Rectangle2D size = screen.getBounds();
		Rectangle2D vsize = screen.getVisualBounds();
		Rectangle2D scale = new Rectangle2D( 0, 0, screen.getOutputScaleX(), screen.getOutputScaleY() );
		int dpi = (int)screen.getDpi();

		String sizeText = TextUtil.justify( TextUtil.RIGHT, (int)size.getWidth() + "x" + (int)size.getHeight(), 10 );
		String vsizeText = TextUtil.justify( TextUtil.RIGHT, (int)vsize.getWidth() + "x" + (int)vsize.getHeight(), 10 );
		String scaleText = scale.getWidth() + "x" + scale.getWidth();

		return (isPrimary ? "p" : "s") + "-screen: " + scaleText + " [" + dpi + "dpi] " + sizeText + "\n";
	}

	private String getOperatingSystemDetail() {
		StringBuilder builder = new StringBuilder();

		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
		builder.append( "Name:        " + bean.getName() ).append( "\n" );
		builder.append( "Arch:        " + bean.getArch() ).append( "\n" );
		builder.append( "Version:     " + bean.getVersion() ).append( "\n" );
		builder.append( "Processors:  " + bean.getAvailableProcessors() ).append( "\n" );

		return builder.toString();
	}

	private String getExecutionDetail( Program program ) {
		StringBuilder builder = new StringBuilder();

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		long uptime = bean.getUptime();
		builder.append( "Start time:        " + DateUtil.format( new Date( bean.getStartTime() ), DateUtil.DEFAULT_DATE_FORMAT ) ).append( "\n" );
		builder.append( "Current time:      " + DateUtil.format( new Date(), DateUtil.DEFAULT_DATE_FORMAT ) ).append( "\n" );
		builder.append( "Uptime:            " + DateUtil.formatDuration( uptime ) ).append( "\n" );

		long lastUpdateCheck = program.getProductManager().getLastUpdateCheck();
		long nextUpdateCheck = program.getProductManager().getNextUpdateCheck();
		if( nextUpdateCheck < System.currentTimeMillis() ) nextUpdateCheck = 0;

		String unknown = program.getResourceBundle().getString( BundleKey.UPDATE, "unknown" );
		String notScheduled = program.getResourceBundle().getString( BundleKey.UPDATE, "not-scheduled" );
		builder.append( "\n" );
		builder
			.append( "Last update check: " + (lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT )) )
			.append( "\n" );
		builder
			.append( "Next update check: " + (nextUpdateCheck == 0 ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT )) )
			.append( "\n" );

		return builder.toString();
	}

	private String getMemoryDetail() {
		StringBuilder builder = new StringBuilder();

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		builder.append( "Heap use:     " + bean.getHeapMemoryUsage() ).append( "\n" );
		builder.append( "Non-heap use: " + bean.getNonHeapMemoryUsage() ).append( "\n" );

		builder.append( "\n" );
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		for( MemoryPoolMXBean pool : pools ) {
			builder.append( pool.getName() + " (" + pool.getType() + "): " + pool.getUsage() ).append( "\n" );
		}

		return builder.toString();
	}

	private String getThreadDetail() {
		StringBuilder builder = new StringBuilder();

		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		builder.append( "Highest thread count:        " + bean.getPeakThreadCount() ).append( "\n" );
		builder.append( "Current thread count:        " + bean.getThreadCount() ).append( "\n" );

		builder.append( "\n" );
		List<ThreadInfo> threads = Arrays.asList( bean.getThreadInfo( bean.getAllThreadIds() ) );
		Collections.sort( threads, new ThreadInfoNameComparator() );
		for( ThreadInfo thread : threads ) {
			builder.append( TextUtil.leftJustify( thread.getThreadState().toString(), 15 ) );
			builder.append( "  " );
			builder.append( thread.getThreadName() );
			builder.append( "\n" );
		}

		return builder.toString();
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.size() > 0 ) selectPage( newNodes.iterator().next().getId() );
	}

	private void selectPage( String item ) {
		getChildren().clear();
		getChildren().add( pages.getOrDefault( item, pages.get( SUMMARY ) ) );
	}

	private String getProperties( Properties properties ) {
		StringBuilder builder = new StringBuilder();

		// Load the keys into a list.
		int keyColumnWidth = 0;
		List<String> keys = new ArrayList<>();
		for( Object object : properties.keySet() ) {
			String key = object.toString();
			keys.add( key );
			keyColumnWidth = Math.max( keyColumnWidth, key.length() );
		}

		// Sort the key list
		Collections.sort( keys );
		keyColumnWidth += 2;

		for( String key : keys ) {
			String value = properties.getProperty( key );

			if( key.endsWith( ".path" ) ) {
				String[] elements = value.split( File.pathSeparator );

				// Append the key with the first element.
				builder.append( TextUtil.leftJustify( key, keyColumnWidth ) );
				builder.append( TextUtil.toPrintableString( elements[ 0 ] ) );
				builder.append( "\n" );

				// Append the remaining elements with padding.
				int count = elements.length;
				for( int index = 1; index < count; index++ ) {
					builder.append( TextUtil.pad( keyColumnWidth ) );
					builder.append( TextUtil.toPrintableString( elements[ index ] ) );
					builder.append( "\n" );
				}
			} else {
				builder.append( TextUtil.leftJustify( key, keyColumnWidth ) );
				builder.append( TextUtil.toPrintableString( value ) );
				builder.append( "\n" );
			}
		}

		return builder.toString();
	}

}
