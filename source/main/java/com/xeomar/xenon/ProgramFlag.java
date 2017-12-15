package com.xeomar.xenon;

public interface ProgramFlag {

	String EXECMODE = "execmode";

	String EXECMODE_DEVL = ExecMode.DEV.name().toLowerCase();

	String EXECMODE_TEST = ExecMode.TEST.name().toLowerCase();

	String HOME = "home";

	String LOG_LEVEL = "log-level";

	String NOUPDATE = "noupdate";

	String NOUPDATECHECK = "noupdatecheck";

	String RESET = "reset";

}
