package com.avereon.xenon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( MockitoExtension.class )
class WorkspaceManagerTest extends BasePartXenonTestCase {

	private WorkspaceManager workspaceManager;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
		workspaceManager = new WorkspaceManager( getProgram() );
	}

	@Test
	void getWorkspaces() {
		assertThat( workspaceManager.getWorkspaces() ).hasSize( 0 );
	}

}
