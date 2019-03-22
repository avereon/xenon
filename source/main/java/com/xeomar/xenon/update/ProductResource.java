package com.xeomar.xenon.update;

import com.xeomar.util.UriUtil;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class ProductResource {

	public enum Type {
		FILE, PACK
	}

	private ProductResource.Type type;

	private URI uri;

	private String name;

	private Future<Download> future;

	private Path file;

	private Throwable throwable;

	public ProductResource( ProductResource.Type type, Path file ) {
		this.type = type;
		this.uri = file.toUri();
		this.file = file;
		this.name = file.getFileName().toString();
	}

	public ProductResource( ProductResource.Type type, URI uri ) {
		this.type = type;
		this.uri = uri;
		this.name = UriUtil.parseName( uri );
	}

	public ProductResource.Type getType() {
		return type;
	}

	public URI getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}

	public void waitFor() throws InterruptedException, ExecutionException {
		if( file == null && future != null ) file = future.get().getTarget();
	}

	public Path getLocalFile() {
		return file;
	}

	public void setFuture( Future<Download> future ) {
		this.future = future;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable( Throwable throwable ) {
		this.throwable = throwable;
	}

	public boolean isValid() {
		return throwable == null;
	}

	@Override
	public String toString() {
		return type.name() + ": " + uri;
	}

	private String parseName( URI uri ) {
		return null;
	}
}
