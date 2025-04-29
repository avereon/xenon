package com.avereon.xenon.product;

import com.avereon.xenon.BaseFullXenonTestCase;
import com.avereon.xenon.Module;
import com.avereon.xenon.mod.MockMod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductManagerModLifecycleTest extends BaseFullXenonTestCase {

	private MockMod module;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		module = new MockMod();
		getProgram().getProductManager().registerMod( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.UNREGISTERED );
	}

	@Test
	void testModuleRegisterWithDisabledMod() {
		getProgram().getProductManager().callModRegister( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.REGISTERED );
	}

	@Test
	void testModuleRegisterWithEnabledMod() {
		enableMod();
		getProgram().getProductManager().callModRegister( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.REGISTERED );
	}

	@Test
	void testModuleStartWithDisabledMod() {
		getProgram().getProductManager().callModStart( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.REGISTERED );
	}

	@Test
	void testModuleStartWithEnabledUnregistered() {
		enableMod();
		getProgram().getProductManager().callModStart( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.STARTED );
	}

	@Test
	void testModuleStartWithEnabledRegistered() {
		enableMod();
		getProgram().getProductManager().callModRegister( module );
		getProgram().getProductManager().callModStart( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.STARTED );
	}

	@Test
	void testModuleStopWithDisabledUnregisteredMod() {
		getProgram().getProductManager().callModShutdown( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.UNREGISTERED );
	}

	@Test
	void testModuleStopWithDisabledRegisteredMod() {
		getProgram().getProductManager().callModRegister( module );
		getProgram().getProductManager().callModShutdown( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.REGISTERED );
	}

	@Test
	void testModuleStopWithEnabledStartedMod() {
		enableMod();
		getProgram().getProductManager().callModStart( module );
		getProgram().getProductManager().callModShutdown( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.STOPPED );
	}

	@Test
	void testFullLifecycle() {
		enableMod();
		getProgram().getProductManager().callModRegister( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.REGISTERED );
		getProgram().getProductManager().callModStart( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.STARTED );
		getProgram().getProductManager().callModShutdown( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.STOPPED );
		getProgram().getProductManager().callModUnregister( module );
		assertThat( module.getStatus() ).isEqualTo( Module.Status.UNREGISTERED );
	}

	private void enableMod(){
		getProgram().getProductManager().setModEnabled( module, true );
	}

}
