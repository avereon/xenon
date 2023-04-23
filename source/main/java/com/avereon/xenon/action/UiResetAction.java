package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.RestartHook;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class UiResetAction extends ProgramAction {

  public UiResetAction( Program program ) {
    super( program );
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void handle( ActionEvent event ) {
    // Reset the UI settings before restarting
    getProgram().getSettingsManager().getSettings(ProgramSettings.UI).delete();

    // Restart the application
    try {
      getProgram().requestRestart( RestartHook.Mode.RESTART );
    } catch( Throwable throwable ) {
      log.atError( throwable ).log( "Error requesting restart" );
    }
  }

}
