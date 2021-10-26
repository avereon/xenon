package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.log.Log;
import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.*;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.product.ModEvent;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import lombok.CustomLog;

import java.io.File;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class AboutTool extends GuidedTool {

	public static final String SUMMARY = "summary";

	public static final String DETAILS = "details";

	public static final String MODS = "mods";

	private static final double ICON_SIZE = 96;

	private final Map<String, Node> pages;

	private final SummaryPane summaryPane;

	private TextArea summaryText;

	private BorderPane modsPane;

	private TextArea modsText;

	private BorderPane detailsPane;

	private TextArea detailsText;

	private String currentPageId;

	private EventHandler<SettingsEvent> updateCheckWatcher;

	private EventHandler<ModEvent> modEnabledWatcher;

	public AboutTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-about" );

		summaryPane = new SummaryPane();

		detailsText = new TextArea();
		detailsText.setId( "tool-about-details" );
		detailsText.setEditable( false );
		detailsPane = new BorderPane();
		detailsPane.setCenter( detailsText );

		modsText = new TextArea();
		modsText.setId( "tool-about-mods" );
		modsText.setEditable( false );
		modsPane = new BorderPane();
		modsPane.setCenter( modsText );

		pages = new ConcurrentHashMap<>();
		pages.put( SUMMARY, summaryPane );
		pages.put( DETAILS, detailsPane );
		pages.put( MODS, modsPane );

		Guide guide = createGuide();
		getGuideContext().getGuides().add( guide );
		getGuideContext().setCurrentGuide( guide );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getProduct().getCard().getName() );
		setGraphic( getProgram().getIconLibrary().getIcon( "about" ) );

		updateCheckWatcher = e -> Platform.runLater( summaryPane::updateUpdateCheckInfo );
		getProgram().getSettings().register( ProductManager.LAST_CHECK_TIME, updateCheckWatcher );
		getProgram().getSettings().register( ProductManager.NEXT_CHECK_TIME, updateCheckWatcher );

		modEnabledWatcher = e -> Platform.runLater( this::updatePages );
		getProgram().register( ModEvent.ENABLED, modEnabledWatcher );
		getProgram().register( ModEvent.DISABLED, modEnabledWatcher );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		updatePages();

		// TODO Can this be generalized in GuidedTool?
		String pageId = request.getFragment();
		if( pageId == null ) pageId = currentPageId;
		if( pageId == null ) pageId = SUMMARY;
		selectPage( pageId );
	}

	@Override
	protected void deallocate() throws ToolException {
		getProgram().unregister( ModEvent.ENABLED, modEnabledWatcher );
		getProgram().unregister( ModEvent.DISABLED, modEnabledWatcher );
		getProgram().getSettings().unregister( ProductManager.LAST_CHECK_TIME, updateCheckWatcher );
		getProgram().getSettings().unregister( ProductManager.NEXT_CHECK_TIME, updateCheckWatcher );
		super.deallocate();
	}

	private void updatePages() {
		ProductCard metadata = getProgram().getCard();
		summaryPane.update( metadata );
		modsText.setText( getModsText( (Program)getProduct() ) );
		detailsText.setText( getDetailsText( (Program)getProduct() ) );
	}

	private Guide createGuide() {
		Guide guide = new Guide();

		GuideNode summaryNode = new GuideNode( getProgram(), AboutTool.SUMMARY, Rb.text( "tool", "about-summary" ), "about" );
		GuideNode detailsNode = new GuideNode( getProgram(), AboutTool.DETAILS, Rb.text( "tool", "about-details" ), "about" );
		GuideNode productsNode = new GuideNode( getProgram(), AboutTool.MODS, Rb.text( "tool", "about-mods" ), "about" );

		summaryNode.setOrder( 0 );
		detailsNode.setOrder( 1 );
		productsNode.setOrder( 2 );

		guide.addNode( summaryNode );
		guide.addNode( detailsNode );
		guide.addNode( productsNode );

		return guide;
	}

	private class SummaryPane extends HBox {

		private final Label productName;

		private final Label productVersion;

		private final Label productProvider;

		private final Label javaLabel;

		private Label javaName;

		private final Label javaVmName;

		private final Label javaVersion;

		private final Label javaProvider;

		private final Label osLabel;

		private final Label osName;

		private final Label osVersion;

		private final Label osProvider;

		private Label informationLabel;

		private final String lastUpdateCheckPrompt;

		private final Label lastUpdateTimestamp;

		private final String nextUpdateCheckPrompt;

		private final Label nextUpdateTimestamp;

		public SummaryPane() {
			super( UiFactory.PAD );
			setId( "tool-about-summary" );

			lastUpdateCheckPrompt = Rb.text( BundleKey.UPDATE, "product-update-check-last" );
			nextUpdateCheckPrompt = Rb.text( BundleKey.UPDATE, "product-update-check-next" );
			lastUpdateTimestamp = makeLabel( "tool-about-version" );
			nextUpdateTimestamp = makeLabel( "tool-about-version" );
			javaLabel = makeLabel( "tool-about-header" );
			javaVmName = makeLabel( "tool-about-name" );
			osName = makeLabel( "tool-about-name" );

			getChildren().add( getProgram().getIconLibrary().getIcon( "program", ICON_SIZE ) );

			VBox header = new VBox( UiFactory.PAD );
			header.getChildren().add( productName = makeLabel( "tool-about-title" ) );
			header.getChildren().add( productVersion = makeLabel( "tool-about-version" ) );
			header.getChildren().add( productProvider = makeLabel( "tool-about-provider" ) );
			//			header.getChildren().add( makeSeparator() );
			//			header.getChildren().add( lastUpdateTimestamp );
			//			header.getChildren().add( nextUpdateTimestamp );
			header.getChildren().add( makeSeparator() );
			header.getChildren().add( makeSeparator() );

			VBox information = new VBox( UiFactory.PAD );
			//information.getChildren().add( makeSeparator() );
			//information.getChildren().add( makeSeparator() );
			information.getChildren().add( javaLabel );
			//information.getChildren().add( javaName );
			//information.getChildren().add( javaVmName );
			information.getChildren().add( javaVersion = makeLabel( "tool-about-version" ) );
			information.getChildren().add( javaProvider = makeLabel( "tool-about-provider" ) );
			information.getChildren().add( makeSeparator() );
			information.getChildren().add( makeSeparator() );
			information.getChildren().add( osLabel = makeLabel( "tool-about-header" ) );
			//information.getChildren().add( osName );
			information.getChildren().add( osVersion = makeLabel( "tool-about-version" ) );
			information.getChildren().add( osProvider = makeLabel( "tool-about-provider" ) );

			// Mods
			VBox mods = new VBox( UiFactory.PAD );
			for( ProductCard card : getProgram().getProductManager().getInstalledProductCards( false ) ) {
				if( card.getProductKey().equals( getProgram().getCard().getProductKey() ) ) continue;

				Label modHeader = makeLabel( "tool-about-header" );
				Label modVersion = makeLabel( "tool-about-version" );
				Label modProvider = makeLabel( "tool-about-provider" );

				modHeader.setText( card.getName() );
				modVersion.setText( card.getVersion() );
				modProvider.setText( card.getProvider() );

				mods.getChildren().add( modHeader );
				mods.getChildren().add( modVersion );
				mods.getChildren().add( modProvider );
				mods.getChildren().add( makeSeparator() );
				mods.getChildren().add( makeSeparator() );
			}

			HBox hLayout = new HBox( 10 * UiFactory.PAD, information, mods );
			VBox vLayout = new VBox( header, hLayout );

			getChildren().addAll( vLayout );
		}

		public void update( ProductCard card ) {
			String from = Rb.text( "tool", "about-from" );
			productName.setText( card.getName() );
			if( card.getRelease().getVersion().isSnapshot() ) {
				productVersion.setText( card.getRelease().toHumanString( TimeZone.getDefault() ) );
			} else {
				productVersion.setText( card.getRelease().getVersion().toHumanString() );
			}
			productProvider.setText( from + " " + card.getProvider() );

			javaLabel.setText( "Java " + System.getProperty( "java.version" ) );
			javaVmName.setText( System.getProperty( "java.vm.name" ) );
			String javaVersionDate = System.getProperty( "java.version.date" );
			if( javaVersionDate == null ) {
				javaVersion.setText( System.getProperty( "java.vendor.version" ) );
			} else {
				javaVersion.setText( System.getProperty( "java.vendor.version" ) + "  " + javaVersionDate );
			}
			javaProvider.setText( from + " " + System.getProperty( "java.vendor" ) );

			String osNameString = System.getProperty( "os.name" );
			//osLabel.setText( Rb.text( "tool", "about-system" ) );
			osLabel.setText( osNameString.substring( 0, 1 ).toUpperCase() + osNameString.substring( 1 ) );
			osVersion.setText( OperatingSystem.getVersion() );
			osProvider.setText( from + " " + OperatingSystem.getProvider() );

			//informationLabel.setText( Rb.text( BundleKey.LABEL, "information" ) );
			updateUpdateCheckInfo();
		}

		private void updateUpdateCheckInfo() {
			long lastUpdateCheck = getProgram().getProductManager().getLastUpdateCheck();
			long nextUpdateCheck = getProgram().getProductManager().getNextUpdateCheck();
			if( nextUpdateCheck < System.currentTimeMillis() ) nextUpdateCheck = 0;

			String unknown = Rb.text( BundleKey.UPDATE, "unknown" );
			String notScheduled = Rb.text( BundleKey.UPDATE, "not-scheduled" );
			String lastUpdateCheckText = lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );
			String nextUpdateCheckText = nextUpdateCheck == 0 ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );

			Platform.runLater( () -> {
				lastUpdateTimestamp.setText( lastUpdateCheckPrompt + "  " + lastUpdateCheckText );
				nextUpdateTimestamp.setText( nextUpdateCheckPrompt + "  " + nextUpdateCheckText );
			} );
		}

	}

	private Node makeSeparator() {
		return new Pane();
	}

	private Label makeLabel( String... styleClass ) {
		Label label = new Label();
		label.getStyleClass().addAll( styleClass );
		return label;
	}

	private String getModsText( Program program ) {
		StringBuilder builder = new StringBuilder();

		program
			.getProductManager()
			.getInstalledProductCards( false )
			.stream()
			.filter( ( card ) -> !card.getProductKey().equals( program.getCard().getProductKey() ) )
			.forEach( ( card ) -> builder.append( getProductDetails( card ) ).append( "\n" ) );

		return builder.toString();
	}

	@SuppressWarnings( "StringBufferReplaceableByString" )
	private String getDetailsText( Program program ) {
		ProductCard metadata = program.getCard();
		StringBuilder builder = new StringBuilder();

		// Program details
		builder.append( getHeader( "Program: " + metadata.getName() + " " + metadata.getVersion() ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getProductDetails( program.getCard() ), 4, " " ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getProgramDetails( program ), 4, " " ) );

		// JavaFX
		builder.append( "\n" );
		builder.append( getHeader( "JavaFX information" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getJavaFxDetail(), 4, " " ) );

		// Runtime
		builder.append( "\n" );
		builder.append( getHeader( "Runtime information" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getRuntimeDetail( program ), 4, " " ) );

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

		// Java modules
		builder.append( "\n" );
		builder.append( getHeader( "Java modules" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( getJavaModuleDetail(), 4, " " ) );

		// System properties
		builder.append( "\n" );
		builder.append( getHeader( "System properties" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( toOrderedMap( System.getProperties() ), 4, " " ) );

		// Environment variables
		builder.append( "\n" );
		builder.append( getHeader( "Environment variables" ) );
		builder.append( "\n" );
		builder.append( Indenter.indent( toOrderedMap( System.getenv() ), 4, " " ) );

		//		// Settings
		//		builder.append( "\n" );
		//		builder.append( getHeader( "Settings" ) );
		//		builder.append( "\n" );
		//		builder.append( Indenter.indent( getSettingsDetail(), 4, " " ) );

		// The current date
		builder.append( "\n" );
		builder.append( getHeader( "About details timestamp: " + DateUtil.format( new Date(), DateUtil.DEFAULT_DATE_FORMAT ) ) );

		return builder.toString();
	}

	private String getHeader( String text ) {
		return "--- " + text + "\n";
	}

	@SuppressWarnings( "StringBufferReplaceableByString" )
	private String getProgramDetails( Program program ) {
		StringBuilder builder = new StringBuilder();

		builder.append( "Home folder: " ).append( program.getHomeFolder() ).append( "\n" );
		builder.append( "Data folder: " ).append( program.getDataFolder() ).append( "\n" );
		builder.append( "User folder: " ).append( System.getProperty( "user.home" ) ).append( "\n" );
		builder.append( "Log file:    " ).append( Log.getLogFile() ).append( "\n" );
		builder.append( "Launcher:    " ).append( OperatingSystem.getJavaLauncherPath() ).append( "\n" );
		builder.append( "Updater:     " ).append( program.getUpdateManager().getUpdaterLauncher() ).append( "\n" );

		return builder.toString();
	}

	private String getProductDetails( ProductCard card ) {
		StringBuilder builder = new StringBuilder();

		builder.append( "Product:     " ).append( card.getName() ).append( "\n" );
		builder.append( "Provider:    " ).append( card.getProvider() ).append( "\n" );
		builder.append( "Inception:   " ).append( card.getInception() ).append( "\n" );
		builder.append( "Summary:     " ).append( card.getSummary() ).append( "\n" );

		builder.append( "Group:       " ).append( card.getGroup() ).append( "\n" );
		builder.append( "Artifact:    " ).append( card.getArtifact() ).append( "\n" );
		builder.append( "Version:     " ).append( card.getVersion() ).append( "\n" );
		builder.append( "Timestamp:   " ).append( card.getTimestamp() ).append( "\n" );

		ProductManager productManager = getProgram().getProductManager();
		builder.append( "Enabled:     " ).append( productManager.isEnabled( card ) ).append( "\n" );
		//		builder.append( "Updatable:   " ).append( productManager.isUpdatable( card )).append( "\n" );
		//		builder.append( "Removable:   " ).append( productManager.isRemovable( card )).append( "\n" );

		return builder.toString();
	}

	private String getRuntimeDetail( Program program ) {
		StringBuilder builder = new StringBuilder();

		// CPU Information
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
		builder.append( "CPU Cores:        " ).append( bean.getAvailableProcessors() ).append( "\n" );

		// JVM Memory
		long max = Runtime.getRuntime().maxMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - Runtime.getRuntime().freeMemory();
		builder
			.append( "JVM Memory:       " )
			.append( FileUtil.getHumanSizeBase2( used ) )
			.append( " / " )
			.append( FileUtil.getHumanSizeBase2( total ) )
			.append( " / " )
			.append( FileUtil.getHumanSizeBase2( max ) )
			.append( "\n" );

		// JVM commands
		builder.append( "\n" );
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		builder.append( "JVM commands:     " ).append( TextUtil.toString( runtimeMXBean.getInputArguments(), " " ) ).append( "\n" );

		// Program commands
		Parameters parameters = program.getProgramParameters();
		builder.append( "Program commands: " ).append( parameters == null ? "" : TextUtil.toString( parameters.getOriginalCommands(), " " ) ).append( "\n" );

		return builder.toString();
	}

	private String getJavaFxDetail() {
		StringBuilder builder = new StringBuilder();

		// Is the program hardware rendered
		boolean hardwareRendered = getProgram().isHardwareRendered();
		builder.append( "Hardware rendered: " ).append( hardwareRendered ).append( "\n" );
		if( !hardwareRendered ) builder.append( "  Consider adding the following JVM args: -Dprism.forceGPU=true\n" );

		// The screen information
		builder.append( "\n" );
		Screen primary = Screen.getPrimary();
		Screen.getScreens().forEach( ( screen ) -> builder.append( getScreenDetail( primary, screen ) ) );

		return builder.toString();
	}

	private String getScreenDetail( Screen primary, Screen screen ) {
		boolean isPrimary = primary.hashCode() == screen.hashCode();
		Rectangle2D size = screen.getBounds();

		Scene scene = getScene();
		Rectangle2D outputScale = new Rectangle2D( 0, 0, screen.getOutputScaleX(), screen.getOutputScaleY() );
		Rectangle2D renderScale = scene == null ? null : new Rectangle2D( 0, 0, scene.getWindow().getRenderScaleX(), scene.getWindow().getRenderScaleY() );
		Rectangle2D sceneScale = scene == null ? null : new Rectangle2D( 0, 0, scene.getRoot().getScaleX(), scene.getRoot().getScaleY() );
		int dpi = (int)screen.getDpi();

		String sizeText = TextUtil.justify( TextUtil.RIGHT, (int)size.getWidth() + "x" + (int)size.getHeight(), 10 );
		String scaleText = outputScale.getWidth() + "x" + outputScale.getWidth();
		String renderScaleText = renderScale == null ? "" : renderScale.getWidth() + "x" + renderScale.getWidth();
		String sceneScaleText = sceneScale == null ? "" : sceneScale.getWidth() + "x" + sceneScale.getWidth();

		//System.out.println( "Screen scale: " + scaleText );
		//System.out.println( "Render scale: " + renderScaleText );
		//System.out.println( "Scene scale: " + sceneScaleText );

		return (isPrimary ? "p" : "s") + "-screen: " + renderScaleText + " [" + dpi + "dpi] " + sizeText + "\n";
	}

	private String getOperatingSystemDetail() {
		StringBuilder builder = new StringBuilder();

		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
		builder.append( "Name:        " ).append( bean.getName() ).append( "\n" );
		builder.append( "Arch:        " ).append( bean.getArch() ).append( "\n" );
		builder.append( "Version:     " ).append( bean.getVersion() ).append( "\n" );
		builder.append( "Processors:  " ).append( bean.getAvailableProcessors() ).append( "\n" );

		return builder.toString();
	}

	private String getExecutionDetail( Program program ) {
		StringBuilder builder = new StringBuilder();

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		long uptime = bean.getUptime();
		builder.append( "Start time:        " ).append( DateUtil.format( new Date( bean.getStartTime() ), DateUtil.DEFAULT_DATE_FORMAT ) ).append( "\n" );
		builder.append( "Current time:      " ).append( DateUtil.format( new Date(), DateUtil.DEFAULT_DATE_FORMAT ) ).append( "\n" );
		builder.append( "Uptime:            " ).append( DateUtil.formatDuration( uptime ) ).append( "\n" );

		String lastUpdateCheck = program.getProductManager().getLastUpdateCheckText();
		String nextUpdateCheck = program.getProductManager().getNextUpdateCheckText();

		builder.append( "\n" );
		builder.append( "Last update check: " ).append( lastUpdateCheck ).append( "\n" );
		builder.append( "Next update check: " ).append( nextUpdateCheck ).append( "\n" );

		return builder.toString();
	}

	private String getMemoryDetail() {
		StringBuilder builder = new StringBuilder();

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		builder.append( "Heap use:     " ).append( bean.getHeapMemoryUsage() ).append( "\n" );
		builder.append( "Non-heap use: " ).append( bean.getNonHeapMemoryUsage() ).append( "\n" );

		builder.append( "\n" );
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		for( MemoryPoolMXBean pool : pools ) {
			builder.append( pool.getName() ).append( " (" ).append( pool.getType() ).append( "): " ).append( pool.getUsage() ).append( "\n" );
		}

		return builder.toString();
	}

	private String getThreadDetail() {
		StringBuilder builder = new StringBuilder();

		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		builder.append( "Highest thread count:        " ).append( bean.getPeakThreadCount() ).append( "\n" );
		builder.append( "Current thread count:        " ).append( bean.getThreadCount() ).append( "\n" );

		builder.append( "\n" );
		List<ThreadInfo> threads = Arrays.asList( bean.getThreadInfo( bean.getAllThreadIds() ) );
		threads.sort( new ThreadInfoNameComparator() );
		for( ThreadInfo thread : threads ) {
			builder.append( TextUtil.leftJustify( thread.getThreadState().toString(), 15 ) );
			builder.append( "  " );
			builder.append( thread.getThreadName() );
			builder.append( "\n" );
		}

		return builder.toString();
	}

	private String getJavaModuleDetail() {
		StringBuilder builder = new StringBuilder();

		builder.append( "Mod layers:\n" );
		getProgram().getProductManager().getModules().stream().sorted().forEach( m -> builder.append( m.getClass().getModule().getName() ).append( "\n" ) );

		// Java modules
		builder.append( "\n" );
		builder.append( "Boot layer:\n" );
		ModuleLayer.boot().modules().stream().sorted( Comparator.comparing( Module::getName ) ).forEach( m -> builder.append( m.getName() ).append( "\n" ) );

		return builder.toString();
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.size() > 0 ) selectPage( newNodes.iterator().next().getId() );
	}

	private void selectPage( String pageId ) {
		currentPageId = pageId;
		if( pageId == null ) return;

		Node page = pages.get( pageId );
		if( page == null ) page = pages.get( SUMMARY );

		getChildren().clear();
		getChildren().add( page );
	}

	private String toOrderedMap( Map<?, ?> map ) {
		StringBuilder builder = new StringBuilder();

		// Load the keys into a list.
		int keyColumnWidth = 0;
		List<String> keys = new ArrayList<>();
		for( Object object : map.keySet() ) {
			String key = object.toString();
			keys.add( key );
			keyColumnWidth = Math.max( keyColumnWidth, key.length() );
		}

		// Sort the key list
		Collections.sort( keys );
		keyColumnWidth += 2;

		for( String key : keys ) {
			String value = String.valueOf( map.get( key ) );

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
