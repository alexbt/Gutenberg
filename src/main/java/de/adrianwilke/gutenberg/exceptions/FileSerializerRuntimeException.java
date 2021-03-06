package de.adrianwilke.gutenberg.exceptions;

import java.io.IOException;

import de.adrianwilke.gutenberg.io.FileSerializer;

/**
 * Container for exceptions thrown on file serialization.
 * 
 * See: {@link FileSerializer}
 * 
 * @author Adrian Wilke
 */
public class FileSerializerRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileSerializerRuntimeException(IOException e) {
		super(e);
	}

	public FileSerializerRuntimeException(ClassNotFoundException e) {
		super(e);
	}
}