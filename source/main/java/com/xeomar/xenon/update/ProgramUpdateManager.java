package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramFlag;
import com.xeomar.xenon.ProgramTask;
import com.xeomar.xenon.notice.Notice;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProgramUpdateManager extends UpdateManager {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	public ProgramUpdateManager( Program program ) {
		super( program );
		this.program = program;
	}

	// This is a method for testing the update found dialog.
	// It should not be used for production functionality.
	public void showUpdateFoundDialog() {
		new ApplyUpdates( program, Set.of() ).handleApplyUpdates();
	}

	@Override
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		program.getExecutor().submit( new CheckForUpdates( program, interactive ) );
	}

	/**
	 * Return values:
	 * <ul>
	 * <li>-1 : Will not apply updates and will exit the program</li>
	 * <li>0 : Will not apply updates but will still run the program</li>
	 * <li>&gt;0 : Will apply updates and restart the program</li>
	 * </ul>
	 *
	 * @return The number of updates applied or -1 to cancel
	 */
	@Override
	public int userApplyStagedUpdates() {
		if( !isEnabled() || getStagedUpdateCount() == 0 ) return 0;

		/*
		 * If the ProgramFlag.UPDATE_IN_PROGRESS is set that means that the program
		 * was started as a result of a program update and the staged updates can
		 * be cleared.
		 */
		if( program.getProgramParameters().isSet( ProgramFlag.UPDATE_IN_PROGRESS ) ) {
			clearStagedUpdates();
			return 0;
		}

		String programName = program.getCard().getName();
		/*
		 * If the ProgramFlag.UPDATE_IN_PROGRESS is not set, that means the program
		 * was started normally and the user should be asked what to do about the
		 * staged updates. The options are Yes (install the updates and restart the
		 * program), No (do not install the updates, keep them for next time, and
		 * start the program) and Discard (discard the updates and start the program).
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
				return super.userApplyStagedUpdates();
			} else if( result.get() == ButtonType.CANCEL ) {
				return -1;
			} else if( result.get() == discard ) {
				clearStagedUpdates();
				return 0;
			}
		}

		return 0;
	}

	/**
	 * Starts a new task to apply the selected updates.
	 *
	 * @param updates The updates to apply.
	 */
	@Override
	public void applySelectedUpdates( Set<ProductCard> updates ) {
		program.getTaskManager().submit( new ApplyUpdates( program, updates ) );
	}

	private final class CheckForUpdates extends ProgramTask<Void> {

		private boolean interactive;

		private Future<Set<ProductCard>> postedUpdatesFuture;

		CheckForUpdates( Program program, boolean interactive ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-check" ) );
			this.interactive = interactive;
			postedUpdatesFuture = program.getTaskManager().submit( new FindPostedUpdatesTask( program, interactive ) );
		}

		@Override
		public Void call() throws Exception {
			if( !isEnabled() ) return null;

			// Get the installed packs.
			Set<ProductCard> installedPacks = getProductCards();

			// Get the posted updates.
			Set<ProductCard> postedUpdates = null;
			try {
				postedUpdates = postedUpdatesFuture.get();
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

					Platform.runLater( () -> program.getNoticeManager().addNotice( new Notice( title, message ) ) );
				}
				return null;
			}

			if( program.isRunning() ) handleFoundUpdates( installedPacks, postedUpdates, interactive );

			return null;
		}

		private void handleFoundUpdates( Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates, boolean interactive ) {
			if( interactive ) {
				notifyUserOfUpdates( true );
			} else {
				switch( getFoundOption() ) {
					case SELECT: {
						notifyUserOfUpdates( false );
						break;
					}
					case STORE: {
						// Store (download) all updates without user intervention.
						program.getExecutor().submit( new StoreUpdates( program, installedPacks, postedUpdates ) );
						break;
					}
					case APPLY: {
						// Stage all updates without user intervention.
						program.getExecutor().submit( new ApplyUpdates( program, postedUpdates ) );
						break;
					}
				}
			}
		}

		private void notifyUserOfUpdates( boolean interactive ) {
			if( interactive || getFoundOption() == FoundOption.SELECT ) {
				String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
				String header = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found" );
				String message = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found-review" );

				URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.UPDATES );
				Notice notice = new Notice( header, message, () -> program.getResourceManager().open( uri ) );
				program.getNoticeManager().addNotice( notice );
			}
		}

	}

	private final class StoreUpdates extends ProgramTask<Void> {

		private Set<ProductCard> installedPacks;

		private Set<ProductCard> postedUpdates;

		StoreUpdates( Program program, Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-cache-selected" ) );
			this.installedPacks = installedPacks;
			this.postedUpdates = postedUpdates;
		}

		@Override
		public Void call() throws Exception {
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

	//	private final class StageUpdates extends ProgramTask<Integer> {
	//
	//		private Set<ProductCard> selectedUpdates;
	//
	//		StageUpdates( Program program, Set<ProductCard> selectedUpdates ) {
	//			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-selected" ) );
	//			this.selectedUpdates = selectedUpdates;
	//		}
	//
	//		@Override
	//		public Integer call() throws Exception {
	//			return stageUpdates( selectedUpdates );
	//		}
	//
	//	}

	private final class ApplyUpdates extends ProgramTask<Void> {

		private Future<Integer> stageFuture;

		ApplyUpdates( Program program, Set<ProductCard> selectedUpdates ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-apply-selected" ) );
			stageFuture = program.getTaskManager().submit( new StageUpdates( program, selectedUpdates ) );
		}

		@Override
		public Void call() throws Exception {
			if( stageFuture.get() > 0 ) handleApplyUpdates();
			return null;
		}

		/**
		 * Nearly identical to ProductTool.handleStagedUpdates()
		 */
		private void handleApplyUpdates() {
			String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			String header = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
			String message = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

			//			// Example:
			//			URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.UPDATES );
						Notice notice = new Notice( header, message, ProgramUpdateManager.super::userApplyStagedUpdates );
						program.getNoticeManager().addNotice( notice );


			// NEXT Change this to a notice (implemented above)
//			Platform.runLater( () -> {
//				Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
//				alert.setTitle( title );
//				alert.setHeaderText( header );
//				alert.setContentText( message );
//
//				Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
//				Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );
//
//				if( result.isPresent() && result.get() == ButtonType.YES ) ProgramUpdateManager.super.userApplyStagedUpdates();
//			} );
		}

	}

}
