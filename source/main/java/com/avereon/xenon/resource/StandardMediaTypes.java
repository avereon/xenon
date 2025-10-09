package com.avereon.xenon.resource;

public interface StandardMediaTypes {

	// https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
	// NOTE Top level types are: application, audio, font, image, model, video, text

	String APPLICATION_OCTET_STREAM = "application/octet-stream";

	String APPLICATION_ZIP = "application/zip";

	String TEXT_PLAIN = "text/plain";

	/* The following are for convenience */

	String BINARY = APPLICATION_OCTET_STREAM;

	String TEXT = TEXT_PLAIN;

	String DEFAULT = APPLICATION_OCTET_STREAM;

}
