package org.mangocube.corenut.commons.io.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * {@link Resource} implementation for <code>java.io.File</code> handles.
 * Obviously supports resolution as File, and also as URL.
 *
 * @since 1.0
 */
public class FileSystemResource extends AbstractResource {

	private final File file;

	private final String path;


	/**
	 * Create a new FileSystemResource.
	 * @param file a File handle
	 */
	public FileSystemResource(File file) {
		assert (file == null);
		this.file = file;
		this.path = StringUtils4Resource.cleanPath(file.getPath());
	}

	/**
	 * Create a new FileSystemResource.
	 * @param path a file path
	 */
	public FileSystemResource(String path) {
		assert (path == null);
		this.file = new File(path);
		this.path = StringUtils4Resource.cleanPath(path);
	}

	/**
	 * Return the file path for this resource.
	 */
	public final String getPath() {
		return this.path;
	}


	/**
	 * This implementation returns whether the underlying file exists.
	 * @see java.io.File#exists()
	 */
	public boolean exists() {
		return this.file.exists();
	}

	/**
	 * This implementation opens a FileInputStream for the underlying file.
	 * @see java.io.FileInputStream
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	/**
	 * This implementation returns a URL for the underlying file.
	 * @see java.io.File#getAbsolutePath()
	 */
	public URL getURL() throws IOException {
		return file.toURI().toURL();
	}

	/**
	 * This implementation returns the underlying File reference.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * This implementation creates a FileSystemResource, applying the given path
	 * relative to the path of the underlying file of this resource descriptor.
	 * @see org.springframework.util.StringUtils#applyRelativePath(String, String)
	 */
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils4Resource.applyRelativePath(this.path, relativePath);
		return new FileSystemResource(pathToUse);
	}

	/**
	 * This implementation returns the name of the file.
	 * @see java.io.File#getName()
	 */
	public String getFilename() {
		return this.file.getName();
	}

	/**
	 * This implementation returns a description that includes the absolute
	 * path of the file.
	 * @see java.io.File#getAbsolutePath()
	 */
	public String getDescription() {
		return "file [" + this.file.getAbsolutePath() + "]";
	}


	/**
	 * This implementation compares the underlying File references.
	 */
	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof FileSystemResource && this.path.equals(((FileSystemResource) obj).path)));
	}

	/**
	 * This implementation returns the hash code of the underlying File reference.
	 */
	public int hashCode() {
		return this.path.hashCode();
	}

}
