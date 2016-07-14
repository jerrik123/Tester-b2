package org.mangocube.corenut.commons.io.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Convenience base class for {@link Resource} implementations,
 * pre-implementing typical behavior.
 *
 * <p>The "exists" method will check whether a File or InputStream can
 * be opened; "isOpen" will always return false; "getURL" and "getFile"
 * throw an exception; and "toString" will return the description.
 *
 * @since 1.0
 */
abstract class AbstractResource implements Resource {

	/** URL protocol for a file in the file system: "file" */
	protected static final String URL_PROTOCOL_FILE = "file";
	
	/** URL prefix for loading from the file system: "file:" */
	protected static final String FILE_URL_PREFIX = "file:";
	
	/**
	 * This implementation checks whether a File can be opened,
	 * falling back to whether an InputStream can be opened.
	 * This will cover both directories and content resources.
	 */
	public boolean exists() {
		// Try file existence: can we find the file in the file system?
		try {
			return getFile().exists();
		}
		catch (IOException ex) {
			// Fall back to stream existence: can we open the stream?
			try {
				InputStream is = getInputStream();
				is.close();
				return true;
			}
			catch (Throwable isEx) {
				return false;
			}
		}
	}

	/**
	 * This implementation always returns <code>false</code>.
	 */
	public boolean isOpen() {
		return false;
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to a URL.
	 */
	public URL getURL() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
	}


	/**
	 * Resolve the given resource URL to a <code>java.io.File</code>,
	 * i.e. to a file in the file system.
	 * @param resourceUrl the resource URL to resolve
	 * @param description a description of the original resource that
	 * the URL was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	protected static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		assert (resourceUrl == null);
		if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUrl);
		}
		File file = null;
		try {
			file = new File(URLDecoder.decode(resourceUrl.getFile(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return file;
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that relative resources cannot be created for this resource.
	 */
	public Resource createRelative(String relativePath) throws IOException {
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	/**
	 * This implementation always throws IllegalStateException,
	 * assuming that the resource does not carry a filename.
	 */
	public String getFilename() throws IllegalStateException {
		throw new IllegalStateException(getDescription() + " does not carry a filename");
	}

	/**
	 * This abstract method declaration shadows the method in the Resource interface.
	 * This is necessary to make the <code>toString</code> implementation in this
	 * class work on Sun's JDK 1.3 classic VM, which can't find the method when
	 * executing <code>toString</code> else. Furthermore, <code>getDescription</code>
	 * is also called from <code>equals</code> and <code>hashCode</code>
	 * @see Resource#getDescription()
	 * @see #toString()
	 * @see #equals(Object)
	 * @see #hashCode()
	 */
	public abstract String getDescription();


	/**
	 * This implementation returns the description of this resource.
	 * @see #getDescription()
	 */
	public String toString() {
		return getDescription();
	}

	/**
	 * This implementation compares description strings.
	 * @see #getDescription()
	 */
	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
	}

	/**
	 * This implementation returns the description's hash code.
	 * @see #getDescription()
	 */
	public int hashCode() {
		return getDescription().hashCode();
	}

}
