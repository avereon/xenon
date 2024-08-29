package com.avereon.xenon.asset;

import java.nio.file.WatchEvent;

public interface AssetWatchListener {

	void handle( WatchEvent<?> event);

}
