package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramFlag;
import com.xeomar.xenon.util.DialogUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

public class ProgramProductManager extends ProductManager {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	public ProgramProductManager( Program program ) {
		super( program );
		this.program = program;
	}

	@Override
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		// FIXME Return to using interactive flag after testing
		//new UpdateCheckPoc( program ).checkForUpdates( interactive );
		new UpdateCheckPoc( program ).checkForUpdates( getInstalledProductCards(), false );
	}

	/**
	 * Starts a new task to apply the selected updates.
	 *
	 * @param updates The updates to apply.
	 */
	@Override
	public void applySelectedUpdates( Set<ProductCard> updates ) {
		applySelectedUpdates( updates, false );
	}

	public void applySelectedUpdates( Set<ProductCard> updates, boolean interactive ) {
		new UpdateCheckPoc( program ).stageAndApplyUpdates( updates, interactive );
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

}
