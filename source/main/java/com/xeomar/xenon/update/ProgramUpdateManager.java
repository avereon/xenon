package com.xeomar.xenon.update;

import com.xeomar.annex.UpdateFlag;
import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramFlag;
import com.xeomar.xenon.ProgramTask;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.util.DialogUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProgramUpdateManager extends UpdateManager {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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

	/**
	 * @param extras Extra commands to add to the update program when launched.
	 * @return The number of updates applied or -1 to cancel
	 */
	@Override
	public int userApplyStagedUpdates( String... extras ) {
		if( !isEnabled() || getStagedUpdateCount() == 0 ) return 0;

		/*
		 * If the ProgramFlag.UPDATE_IN_PROGRESS is set that means that the program
		 * was started as a result of a program update and the staged updates can
		 * be cleared.
		 */
		if( program.getProgramParameters().isSet( ProgramFlag.UPDATE_IN_PROGRESS ) ) {
			int count = getStagedUpdateCount();
			clearStagedUpdates();
			return count;
		}

		String programName = program.getCard().getName();

		List<String> commandList = new ArrayList<>( Arrays.asList( extras ) );
		commandList.add( UpdateFlag.TITLE );
		commandList.add( program.getResourceBundle().getString( BundleKey.UPDATE, "updater-updating", programName ) );
		String[] commands = commandList.toArray( new String[ 0 ] );

		/*
		 * If the ServiceFlag.NOUPDATECHECK is not set, that means the program was
		 * started normally and the user should be asked what to do about the staged
		 * updates. The options are Yes (install the updates), No (do not install
		 * the updates) and Discard (discard the updates).
		 */
		String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
		String header = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-staged-header", programName );
		String message = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-staged-message" );

		ButtonType discard = new ButtonType( program.getResourceBundle().getString( BundleKey.UPDATE, "updates-discard" ), ButtonBar.ButtonData.LEFT );
		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, message, discard, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
		alert.setGraphic( program.getIconLibrary().getIcon( "update", 64 ) );
		alert.setTitle( title );
		alert.setHeaderText( header );

		// Get the dialog window and set the icons
		Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
		stage.getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );

		Optional<ButtonType> result = DialogUtil.showAndWait( null, alert );

		if( result.isPresent() ) {
			if( result.get() == ButtonType.YES ) {
				return super.applyStagedUpdates( commands );
			} else if( result.get() == ButtonType.CANCEL ) {
				return -1;
			} else if( result.get() == discard ) {
				clearStagedUpdates();
			}
		}

		return 0;
	}

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

		private boolean interactive;

		CheckForUpdates( Program program, boolean interactive ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-check" ) );
			this.interactive = interactive;
		}

		@Override
		public Void call() throws Exception {
			if( !isEnabled() ) return null;

			// Get the installed packs.
			Set<ProductCard> installedPacks = getProductCards();

			// Get the posted updates.
			Set<ProductCard> postedUpdates = null;
			try {
				postedUpdates = findPostedUpdates( interactive );
			} catch( ExecutionException exception ) {
				log.warn( exception.getClass().getName(), exception.getMessage() );
				log.trace( "Error getting posted updates", exception );
			}

			// Notify the user if updates are not available.
			boolean notAvailable = postedUpdates == null || postedUpdates.size() == 0;
			if( notAvailable ) {
				if( interactive ) {
					String title = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates" );
					String updatesNotAvailable = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
					String updatesCannotConnect = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates-source-cannot-connect" );
					final String message = postedUpdates == null ? updatesCannotConnect : updatesNotAvailable;

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

			if( program.isRunning() ) handleFoundUpdates( installedPacks, postedUpdates, interactive );

			return null;
		}

		private void handleFoundUpdates( Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates, boolean interactive ) {
			switch( getFoundOption() ) {
				case SELECT: {
					if( interactive ) {
						// Directly notify the user.
						String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
						String header = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found" );
						String message = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found-review" );

						Platform.runLater( () -> {
							Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
							alert.setTitle( title );
							alert.setHeaderText( header );
							alert.setContentText( message );

							Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
							Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

							if( result.isPresent() && result.get() == ButtonType.YES ) program.getExecutor().submit( this::showUpdates );
						} );
					} else {
						// TODO Use the notice tool to notify the user of posted updates
						String message = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found" );
						//DefaultNotice notice = new DefaultNotice( NoticeKey.UPDATES_FOUND, "notice", message );
						//program.getNoticeManager().submit( notice );
					}
					break;
				}
				case STORE: {
					// Store (download) all updates without user intervention.
					program.getExecutor().submit( new StoreUpdates( installedPacks, postedUpdates ) );
					break;
				}
				case STAGE: {
					// Stage all updates without user intervention.
					program.getExecutor().submit( new StageUpdates( postedUpdates ) );
					break;
				}
			}
		}

		private void showUpdates() {
			try {
				URI uri = URI.create( ProgramProductType.uri + "#" + ProgramProductType.UPDATES );
				program.getResourceManager().open( uri );
			} catch( Exception exception ) {
				log.error( "Error opening artifact tool ", exception );
			}
		}

	}

	private final class StoreUpdates extends ProgramTask<Void> {

		private Set<ProductCard> installedPacks;

		private Set<ProductCard> postedUpdates;

		StoreUpdates( Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-cache-selected" ) );
			this.installedPacks = installedPacks;
			this.postedUpdates = postedUpdates;
		}

		@Override
		public Void call() throws Exception {
			//TaskPanel.showProgress( program, program.getTaskManager(), program.getActiveFrame().getDialogPane() );
			cacheSelectedUpdates( postedUpdates );
			handleCachedUpdates( installedPacks, postedUpdates );
			return null;
		}

		private void handleCachedUpdates( Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates ) {
			String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			Platform.runLater( () -> {
				UpdatesPanel updates = new UpdatesPanel( installedPacks, postedUpdates );

				Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL );
				alert.setTitle( title );
				alert.getDialogPane().setContent( updates );

				Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
				Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

				if( result.isPresent() && result.get() == ButtonType.OK ) program.getExecutor().submit( new StageCachedUpdates( updates.getSelectedUpdates() ) );
			} );
		}

	}

	private final class StageCachedUpdates extends ProgramTask<Void> {

		private Set<ProductCard> selectedUpdates;

		StageCachedUpdates( Set<ProductCard> selectedUpdates ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-cached" ) );
			this.selectedUpdates = selectedUpdates;
		}

		@Override
		public Void call() throws Exception {
			stageCachedUpdates( selectedUpdates );
			return null;
		}

	}

	private final class StageUpdates extends ProgramTask<Void> {

		private Set<ProductCard> selectedUpdates;

		StageUpdates( Set<ProductCard> selectedUpdates ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-selected" ) );
			this.selectedUpdates = selectedUpdates;
		}

		@Override
		public Void call() throws Exception {
			stageUpdates( selectedUpdates );
			handleApplyUpdates( selectedUpdates );
			return null;
		}

		/**
		 * Nearly identical to ProductTool.handleStagedUpdates()
		 * @param selectedUpdates The updates selected by the user
		 */
		private void handleApplyUpdates( Set<ProductCard> selectedUpdates ) {
			if( selectedUpdates.size() == 0 ) return;

			Platform.runLater( () -> {
				String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
				String header = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
				String message = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

				Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
				alert.setTitle( title );
				alert.setHeaderText( header );
				alert.setContentText( message );

				Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
				Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

				if( result.isPresent() && result.get() == ButtonType.YES ) Platform.runLater( () -> program.requestRestart( ProgramFlag.NOUPDATECHECK ) );
			} );
		}

	}

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
