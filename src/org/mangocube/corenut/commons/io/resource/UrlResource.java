package org.mangocube.corenut.commons.io.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * {@link Resource} implementation for <code>java.net.URL</code> locators.
 * Obviously supports resolution as URL, and also as File in case of
 * the "file:" protocol.
 *
 * @since 1.0
 */
public class UrlResource extends AbstractResource {

	/**
	 * Original URL, used for actual access.
	 */
	private final URL url;

	/**
	 * Cleaned URL (with normalized path), used for comparisons.
	 */
	private final URL cleanedUrl;


	/**
	 * Create a new UrlResource.
	 * @param url a URL
	 */
	public UrlResource(URL url) {
		assert (url == null);
		this.url = url;
		this.cleanedUrl = getCleanedUrl(this.url, url.toString());
	}

	/**
	 * Create a new UrlResource.
	 * @param path a URL path
	 * @throws MalformedURLException if the given URL path is not valid
	 */
	public UrlResource(String path) throws MalformedURLException {
		assert (path == null);
		this.url = new URL(path);
		this.cleanedUrl = getCleanedUrl(this.url, path);
	}

	/**
	 * Determine a cleaned URL for the given original URL.
	 * @param originalUrl the original URL
	 * @param originalPath the original URL path
	 * @return the cleaned URL
	 * @see org.springframework.util.StringUtils#cleanPath
	 */
	private URL getCleanedUrl(URL originalUrl, String originalPath) {
		try {
			return new URL(StringUtils4Resource.cleanPath(originalPath));
		}
		catch (MalformedURLException ex) {
			// Cleaned URL path cannot be converted to URL
			// -> take original URL.
			return originalUrl;
		}
	}


	/**
	 * This implementation opens an InputStream for the given URL.
	 * It sets the "UseCaches" flag to <code>false</code>,
	 * mainly to avoid jar file locking on Windows.
	 * @see java.net.URL#openConnection()
	 * @see java.net.URLConnection#setUseCaches(boolean)
	 * @see java.net.URLConnection#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		URLConnection con = this.url.openConnection();
		con.setUseCaches(false);
		return con.getInputStream();
	}

	/**
	 * This implementation returns the underlying URL reference.
	 */
	public URL getURL() throws IOException {
		return this.url;
	}

	/**
	 * This implementation returns a File reference for the underlying URL,
	 * provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	public File getFile() throws IOException {
		return getFile(this.url, getDescription());
	}

	/**
	 * This implementation creates a UrlResource, applying the given path
	 * relative to the path of the underlying URL of this resource descriptor.
	 * @see java.net.URL#URL(java.net.URL, String)
	 */
	public Resource createRelative(String relativePath) throws MalformedURLException {
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		return new UrlResource(new URL(this.url, relativePath));
	}

	/**
	 * This implementation returns the name of the file that this URL refers to.
	 * @see java.net.URL#getFile()
	 * @see java.io.File#getName()
	 */
	public String getFilename() {
		return new File(this.url.getFile()).getName();
	}

	/**
	 * This implementation returns a description that includes the URL.
	 */
	public String getDescription() {
		return "URL [" + this.url + "]";
	}


	/**
	 * This implementation compares the underlying URL references.
	 */
	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource) obj).cleanedUrl)));
	}

	/**
	 * This implementation returns the hash code of the underlying URL reference.
	 */
	public int hashCode() {
		return this.cleanedUrl.hashCode();
	}

}
