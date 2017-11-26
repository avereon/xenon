package com.xeomar.xenon;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.util.DialogUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ProgramUpdateManager extends UpdateManager {

	private static final Logger log = LoggerFactory.getLogger( ProgramUpdateManager.class );

	private Program program;

	public ProgramUpdateManager( Program program ) {
		super( program );
		this.program = program;
	}

	@Override
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		program.getExecutor().submit( new CheckForUpdates( program, interactive ) );
	}

	//	@Override
	//	public int applyStagedUpdates( String... extras ) throws Exception {
	//		if( !isEnabled() || getStagedUpdateCount() == 0 ) return 0;
	//
	//		List<String> commandList = new ArrayList<String>( Arrays.asList( extras ) );
	//		commandList.add( UpdaterFlag.UI );
	//		commandList.add( "true" );
	//		commandList.add( UpdaterFlag.UI_MESSAGE );
	//		commandList.add( ProductUtil.getString( program, BundleKey.MESSAGES, "updater.updating", program.getName() ) );
	//		String[] commands = commandList.toArray( new String[commandList.size()] );
	//
	//		/*
	//		 * If the ServiceFlag.NOUPDATECHECK is set that means that the program was
	//		 * started as a result of a program restart due to staged updates and the
	//		 * updates should be applied without user interaction.
	//		 */
	//		if( program.getParameters().isSet( ServiceFlag.NOUPDATECHECK ) ) return super.applyStagedUpdates( commands );
	//
	//		/*
	//		 * If the ServiceFlag.NOUPDATECHECK is not set, that means the program was
	//		 * started normally and the user should be asked what to do about the staged
	//		 * updates. The options are Yes (install the updates), No (do not install
	//		 * the updates) and Cancel (discard the updates).
	//		 */
	//		Icon icon = program.getIconLibrary().getIcon( "program", 64 );
	//		String title = Bundles.getString( BundleKey.LABELS, "updates" );
	//		String message = MessageFormat.format( Bundles.getString( BundleKey.MESSAGES, "updates.staged" ), program.getCard().getName() );
	//
	//		Object[] options = new Object[3];
	//		options[0] = ProductUtil.getString( program, BundleKey.LABELS, "yes" );
	//		options[1] = ProductUtil.getString( program, BundleKey.LABELS, "no" );
	//		options[2] = ProductUtil.getString( program, BundleKey.LABELS, "updates.discard" );
	//
	//		int result = program.notify( title, message, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon, options, null );
	//
	//		if( result == JOptionPane.YES_OPTION ) {
	//			return super.applyStagedUpdates( commands );
	//		} else if( result == JOptionPane.CANCEL_OPTION ) {
	//			clearStagedUpdates();
	//		}
	//
	//		return 0;
	//	}
	//
	//	private JComponent createUpdatesPanel( Set<ProductCard> installedPacks, Map<String, UpdateOption> updates ) {
	//		JPanel updatesPanel = new JPanel();
	//		updatesPanel.setLayout( new BorderLayout() );
	//
	//		JLabel message = new JLabel( Bundles.getString( BundleKey.MESSAGES, "updates.not.available" ) );
	//		if( updates.size() > 0 ) message = new JLabel( Bundles.getString( BundleKey.MESSAGES, "updates.found" ) );
	//		updatesPanel.add( message, BorderLayout.NORTH );
	//
	//		JPanel packUpdatesPanel = new JPanel();
	//		packUpdatesPanel.setBorder( new EmptyBorder( 20, 0, 0, 0 ) );
	//		packUpdatesPanel.setLayout( new SpringLayout() );
	//		for( ProductCard pack : installedPacks ) {
	//			UpdateOption update = updates.get( pack.getProductKey() );
	//
	//			JCheckBox checkbox = null;
	//			JLabel nameLabel = new JLabel( pack.getName() );
	//			JLabel versionLabel = new JLabel( pack.getRelease().getVersion().toHumanString() );
	//			JLabel timestampLabel = new JLabel( pack.getRelease().getDateString( TimeZone.getDefault() ) );
	//
	//			nameLabel.setFont( nameLabel.getFont().deriveFont( Font.PLAIN ) );
	//			versionLabel.setFont( versionLabel.getFont().deriveFont( Font.PLAIN ) );
	//			timestampLabel.setFont( timestampLabel.getFont().deriveFont( Font.PLAIN ) );
	//
	//			if( update == null ) {
	//				checkbox = new JCheckBox();
	//				checkbox.setEnabled( false );
	//			} else {
	//				checkbox = update.getCheckbox();
	//				checkbox.setSelected( true );
	//
	//				// Use the version and timestamp of the update.
	//				versionLabel.setText( update.getPack().getRelease().getVersion().toHumanString() );
	//				timestampLabel.setText( update.getPack().getRelease().getDateString( TimeZone.getDefault() ) );
	//
	//				// Add the mouse listener to the components.
	//				nameLabel.addMouseListener( new UpdateOptionClickHandler( checkbox ) );
	//				versionLabel.addMouseListener( new UpdateOptionClickHandler( checkbox ) );
	//				timestampLabel.addMouseListener( new UpdateOptionClickHandler( checkbox ) );
	//			}
	//
	//			packUpdatesPanel.add( checkbox );
	//			packUpdatesPanel.add( nameLabel );
	//			packUpdatesPanel.add( versionLabel );
	//			packUpdatesPanel.add( timestampLabel );
	//		}
	//
	//		JComponent updateListComponent;
	//		Layouts.makeGrid( packUpdatesPanel, 4, 10, 0 );
	//		if( updates.size() <= 4 ) {
	//			updateListComponent = packUpdatesPanel;
	//		} else {
	//			JScrollPane scroller = new JScrollPane( packUpdatesPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
	//			Dimension preferredSize = scroller.getPreferredSize();
	//			scroller.setPreferredSize( new Dimension( preferredSize.width + 10, preferredSize.width / 2 + 10 ) );
	//			updateListComponent = scroller;
	//		}
	//		updatesPanel.add( updateListComponent, BorderLayout.CENTER );
	//
	//		return updatesPanel;
	//	}

	private final class CheckForUpdates extends ProgramTask<Void> {

		private boolean forceCheck;

		public CheckForUpdates( Program program, boolean forceCheck ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-check" ) );
			this.forceCheck = forceCheck;
		}

		@Override
		public Void call() throws Exception {
			if( !isEnabled() ) return null;

			// Get the installed packs.
			Set<ProductCard> installedPacks = getProductCards();

			// Get the posted updates.
			Set<ProductCard> postedUpdates = null;
			try {
				postedUpdates = getPostedUpdates( forceCheck );
			} catch( ExecutionException exception ) {
				log.warn( exception.getClass().getName(), exception.getMessage() );
				log.trace( "Error getting posted updates", exception );
			}

			// Notify the user if updates are not available.
			boolean notAvailable = postedUpdates == null || postedUpdates.size() == 0;
			if( notAvailable ) {
				if( forceCheck ) {
					String title = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates" );
					String updatesNotAvailable = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
					String updatesCannotConnect = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates-source-cannot-connect" );
					final String message = postedUpdates == null ? updatesCannotConnect : updatesNotAvailable;

					// FIXME Should switch to the FX thread for the GUI work
					Platform.runLater( () -> {
						Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
						stage.requestFocus();

						Alert alert = new Alert( Alert.AlertType.INFORMATION );
						alert.setTitle( title );
						alert.setHeaderText( "" );
						alert.setContentText( message );
						alert.setDialogPane( alert.getDialogPane() );
						DialogUtil.show( stage, alert );
					} );

				}
				return null;
			}

			//			// Generate the update options.
			//			Map<String, UpdateOption> updateOptions = new HashMap<String, UpdateOption>( postedUpdates.size() );
			//			for( ProductCard pack : postedUpdates ) {
			//				updateOptions.put( pack.getProductKey(), new UpdateOption( pack ) );
			//			}
			//
			//			if( program.isRunning() ) EventQueue.invokeLater( new HandleFoundUpdates( installedPacks, updateOptions, interactive ) );

			return null;
		}

	}

	//	private final class HandleFoundUpdates implements Runnable {
	//
	//		private Set<ProductCard> installedPacks;
	//
	//		boolean interactive;
	//
	//		/**
	//		 * @deprecated This field is deprecated because the CACHESELECT and STAGE
	//		 *             options are not used by the new implementation of product
	//		 *             management using the ProductOrganizer so the user
	//		 *             interactions supported by this field are not used.
	//		 */
	//		@Deprecated
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public HandleFoundUpdates( Set<ProductCard> installedPacks, Map<String, UpdateOption> updateOptions, boolean interactive ) {
	//			this.installedPacks = installedPacks;
	//			this.updateOptions = updateOptions;
	//			this.interactive = interactive;
	//		}
	//
	//		@Override
	//		public void run() {
	//			// Determine the next task.
	//			Task<?> task = null;
	//			switch( getFoundOption() ) {
	//				case SELECT: {
	//					if( interactive ) {
	//						// Directly notify the user.
	//						int updateResult = program.notify( Bundles.getString( BundleKey.LABELS, "updates" ), Bundles.getString( BundleKey.MESSAGES, "updates.found.review" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
	//						if( updateResult == JOptionPane.YES_OPTION ) {
	//							ProductOrganizer tool = (ProductOrganizer)program.getToolManager().openTool( ProductOrganizer.class );
	//							tool.showPage( ProductOrganizer.PAGE_UPDATABLE );
	//						}
	//					} else {
	//						// NEXT Use the notice manager to notify the user.
	//						String message = Bundles.getString( BundleKey.MESSAGES, "updates.found" );
	//						DefaultNotice notice = new DefaultNotice( NoticeKey.UPDATES_FOUND, "notice", message );
	//						program.getNoticeManager().submit( notice );
	//					}
	//					break;
	//				}
	//				case STORE: {
	//					// TODO Store (download) all updates without user intervention.
	//					task = new StoreUpdates( installedPacks, updateOptions );
	//					break;
	//				}
	//				case STAGE: {
	//					// TODO Stage all updates without user intervention.
	//					task = new StageUpdates( updateOptions );
	//					break;
	//				}
	//			}
	//
	//			// Execute the next task.
	//			if( task != null ) program.getTaskManager().submit( task );
	//		}
	//	}
	//
	//	private final class StoreUpdates extends ProgramTask<Void> {
	//
	//		private Set<ProductCard> installedPacks;
	//
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public StoreUpdates( Set<ProductCard> installedPacks, Map<String, UpdateOption> updateOptions ) {
	//			super( program, Bundles.getString( BundleKey.LABELS, "task.updates.cache.selected" ) );
	//			this.installedPacks = installedPacks;
	//			this.updateOptions = updateOptions;
	//		}
	//
	//		@Override
	//		public Void execute() throws Exception {
	//			Set<ProductCard> packs = new HashSet<ProductCard>();
	//
	//			for( UpdateOption update : updateOptions.values() ) {
	//				if( update.isSelected() ) {
	//					packs.add( update.getPack() );
	//				}
	//			}
	//
	//			//TaskPanel.showProgress( program, program.getTaskManager(), program.getActiveFrame().getDialogPane() );
	//			cacheSelectedUpdates( packs );
	//
	//			EventQueue.invokeLater( new HandleCachedUpdates( installedPacks, updateOptions ) );
	//
	//			return null;
	//		}
	//
	//	}
	//
	//	private final class HandleCachedUpdates implements Runnable {
	//
	//		private Set<ProductCard> installedPacks;
	//
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public HandleCachedUpdates( Set<ProductCard> installedPacks, Map<String, UpdateOption> updateOptions ) {
	//			this.installedPacks = installedPacks;
	//			this.updateOptions = updateOptions;
	//		}
	//
	//		@Override
	//		public void run() {
	//			int updateResult = program.notify( Bundles.getString( BundleKey.LABELS, "updates" ), createUpdatesPanel( installedPacks, updateOptions ), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null );
	//			if( updateResult != JOptionPane.OK_OPTION ) return;
	//
	//			Task<?> task = new StageCachedUpdates( updateOptions );
	//			program.getTaskManager().submit( task );
	//		}
	//
	//	}
	//
	//	private final class StageCachedUpdates extends ProgramTask<Void> {
	//
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public StageCachedUpdates( Map<String, UpdateOption> updateOptions ) {
	//			super( program, Bundles.getString( BundleKey.LABELS, "task.updates.stage.cached" ) );
	//			this.updateOptions = updateOptions;
	//		}
	//
	//		@Override
	//		public Void execute() throws Exception {
	//			Set<ProductCard> packs = new HashSet<ProductCard>();
	//
	//			for( UpdateOption update : updateOptions.values() ) {
	//				if( update.isSelected() ) {
	//					packs.add( update.getPack() );
	//				}
	//			}
	//
	//			stageCachedUpdates( packs );
	//
	//			return null;
	//		}
	//
	//	}
	//
	//	private final class StageUpdates extends ProgramTask<Void> {
	//
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public StageUpdates( Map<String, UpdateOption> updateOptions ) {
	//			super( program, Bundles.getString( BundleKey.LABELS, "task.updates.stage.selected" ) );
	//			this.updateOptions = updateOptions;
	//		}
	//
	//		@Override
	//		public Void execute() throws Exception {
	//			Set<ProductCard> cards = new HashSet<ProductCard>();
	//
	//			for( UpdateOption update : updateOptions.values() ) {
	//				if( update.isSelected() ) {
	//					cards.add( update.getPack() );
	//				}
	//			}
	//
	//			stageSelectedUpdates( cards );
	//
	//			EventQueue.invokeLater( new HandleApplyUpdates( updateOptions ) );
	//
	//			return null;
	//		}
	//
	//	}
	//
	//	private final class HandleApplyUpdates implements Runnable {
	//
	//		private Map<String, UpdateOption> updateOptions;
	//
	//		public HandleApplyUpdates( Map<String, UpdateOption> updateOptions ) {
	//			this.updateOptions = updateOptions;
	//		}
	//
	//		@Override
	//		public void run() {
	//			Set<ProductCard> selectedPacks = UpdateOption.getSelectedPacks( updateOptions );
	//
	//			if( selectedPacks.size() == 0 ) return;
	//
	//			int result = program.notify( Bundles.getString( BundleKey.LABELS, "updates" ), Bundles.getString( BundleKey.MESSAGES, "restart.recommended" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null );
	//			if( result == JOptionPane.YES_OPTION ) program.programRestart( true, ProgramFlag.NOUPDATECHECK );
	//		}
	//
	//	}
	//
	//	private static final class UpdateOption implements Comparable<UpdateOption> {
	//
	//		private ProductCard pack;
	//
	//		private JCheckBox checkbox;
	//
	//		public UpdateOption( ProductCard pack ) {
	//			this.pack = pack;
	//			checkbox = new JCheckBox();
	//		}
	//
	//		public ProductCard getPack() {
	//			return pack;
	//		}
	//
	//		public JCheckBox getCheckbox() {
	//			return checkbox;
	//		}
	//
	//		public boolean isSelected() {
	//			return checkbox.isSelected();
	//		}
	//
	//		@Override
	//		public int compareTo( UpdateOption update ) {
	//			return this.pack.getSourceUri().compareTo( update.pack.getSourceUri() );
	//		}
	//
	//		public static final Set<ProductCard> getSelectedPacks( Map<String, UpdateOption> updateOptions ) {
	//			Set<ProductCard> packs = new HashSet<ProductCard>();
	//
	//			for( UpdateOption option : updateOptions.values() ) {
	//				if( option.isSelected() ) packs.add( option.getPack() );
	//			}
	//
	//			return packs;
	//		}
	//
	//	}
	//
	//	private static final class UpdateOptionClickHandler extends MouseAdapter {
	//
	//		private JCheckBox checkbox;
	//
	//		public UpdateOptionClickHandler( JCheckBox checkbox ) {
	//			this.checkbox = checkbox;
	//		}
	//
	//		@Override
	//		public void mousePressed( MouseEvent event ) {
	//			checkbox.dispatchEvent( cloneEvent( event ) );
	//		}
	//
	//		@Override
	//		public void mouseClicked( MouseEvent event ) {
	//			checkbox.dispatchEvent( cloneEvent( event ) );
	//		}
	//
	//		@Override
	//		public void mouseReleased( MouseEvent event ) {
	//			checkbox.dispatchEvent( cloneEvent( event ) );
	//		}
	//
	//		@Override
	//		public void mouseEntered( MouseEvent event ) {
	//			checkbox.dispatchEvent( cloneEvent( event ) );
	//		}
	//
	//		@Override
	//		public void mouseExited( MouseEvent event ) {
	//			checkbox.dispatchEvent( cloneEvent( event ) );
	//		}
	//
	//		private MouseEvent cloneEvent( MouseEvent event ) {
	//			return new MouseEvent( checkbox, event.getID(), event.getWhen(), event.getModifiers(), 0, 0, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), event.getButton() );
	//		}
	//
	//	}

}
