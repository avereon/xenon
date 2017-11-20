package com.xeomar.xenon.update;

import com.xeomar.product.Product;
import com.xeomar.util.XmlDescriptor;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskListener;

import java.io.FileInputStream;
import java.net.URI;

@Deprecated
public class DescriptorDownloadTask extends Task<XmlDescriptor> {

	private DownloadTask task;

	public DescriptorDownloadTask( Product product, URI uri ) {
		super( product.getResourceBundle().getString( "prompt", "download" ) + uri.toString() );
		this.task = new DownloadTask( product, uri );
	}

	public URI getUri() {
		return task.getUri();
	}

	@Override
	public long getMinimum() {
		return task.getMinimum();
	}

	@Override
	public long getMaximum() {
		return task.getMaximum();
	}

	@Override
	public long getProgress() {
		return task.getProgress();
	}

	@Override
	public void addTaskListener( TaskListener listener ) {
		task.addTaskListener( listener );
	}

	@Override
	public void removeTaskListener( TaskListener listener ) {
		task.removeTaskListener( listener );
	}

	@Override
	public XmlDescriptor call() throws Exception {
		Download download = task.call();
		if( download == null ) return null;
		return new XmlDescriptor( new FileInputStream( download.getTarget() ) );
	}

}
