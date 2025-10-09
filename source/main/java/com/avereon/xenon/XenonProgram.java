package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.product.ProductCard;
import com.avereon.product.Program;
import com.avereon.product.ProgramProduct;
import com.avereon.settings.Settings;
import com.avereon.xenon.resource.ResourceManager;
import com.avereon.xenon.resource.ResourceWatchService;
import com.avereon.xenon.index.IndexService;
import com.avereon.xenon.notice.NoticeManager;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.task.TaskManager;
import javafx.stage.Stage;

import java.nio.file.Path;

public interface XenonProgram extends Program, ProgramProduct, XenonProgramProduct {

	// THREAD JavaFX-Launcher
	// EXCEPTIONS Handled by the FX framework
	void init() throws Exception;

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	void start( Stage stage );

	Xenon initForTesting( com.avereon.util.Parameters parameters ) throws Exception;

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	void stop() throws Exception;

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	void requestRestart( RestartJob.Mode mode, String... commands );

	boolean requestExit( boolean skipChecks );

	boolean requestExit( boolean skipVerifyCheck, boolean skipKeepAliveCheck );

	boolean isRunning();

	boolean isHardwareRendered();

	boolean isUpdateInProgress();

	void setUpdateInProgress( boolean updateInProgress );

	com.avereon.util.Parameters getProgramParameters();

	Xenon setProgramParameters( com.avereon.util.Parameters parameters );

	@Override
	Xenon getProgram();

	/**
	 * Get the execution profile.
	 *
	 * @see XenonFlag#PROFILE
	 */
	String getProfile();

	/**
	 * Get the execution mode.
	 *
	 * @see XenonFlag#MODE
	 */
	String getMode();

	/**
	 * Get the home folder. If the home folder is null that means that the program is not installed locally and was most likely started with a technology like
	 * Java Web Start.
	 *
	 * @return The home folder
	 */
	Path getHomeFolder();

	boolean isProgramUpdated();

	@Override
	ProductCard getCard();

	@Override
	Settings getSettings();

	Path getDataFolder();

	Path getLogFolder();

	Path getTempFolder();

	UpdateManager getUpdateManager();

	TaskManager getTaskManager();

	IconLibrary getIconLibrary();

	ActionLibrary getActionLibrary();

	SettingsManager getSettingsManager();

	ToolManager getToolManager();

	ResourceManager getResourceManager();

	ThemeManager getThemeManager();

	WorkspaceManager getWorkspaceManager();

	ProductManager getProductManager();

	NoticeManager getNoticeManager();

	IndexService getIndexService();

	ResourceWatchService getResourceWatchService();

	<T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler );

	<T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler );

	/**
	 * This implementation only returns the product card name.
	 */
	@Override
	String toString();

	Path getHomeFromLauncherPath();

}
