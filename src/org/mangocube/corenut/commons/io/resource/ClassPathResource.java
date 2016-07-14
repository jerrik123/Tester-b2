package org.mangocube.corenut.commons.io.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;

/**
 * {@link Resource} implementation for class path resources.
 * Uses either a given ClassLoader or a given Class for loading resources.
 *
 * <p>Supports resolution as <code>java.io.File</code> if the class path
 * resource resides in the file system, but not for resources in a JAR.
 * Always supports resolution as URL.
 *
 * @since 1.0
 */

@SuppressWarnings("unchecked")
public class ClassPathResource extends AbstractResource {

	private final String path;

	private WeakReference<ClassLoader> classLoader;

	private Class clazz;

	private final static Log logger = LogFactory.getLog(ClassPathResource.class);

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * <p>The thread context class loader will be used for
	 * loading the resource.
	 * @param path the absolute path within the class path
	 * @see java.lang.ClassLoader#getResourceAsStream(String)
	 * @see org.springframework.util.ClassUtilsd#getDefaultClassLoader()
	 */
	public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * @param path the absolute path within the classpath
	 * @param classLoader the class loader to load the resource with,
	 * or <code>null</code> for the thread context class loader
	 * @see java.lang.ClassLoader#getResourceAsStream(String)
	 */
	public ClassPathResource(String path, ClassLoader classLoader) {
		assert (path == null);
		this.path = formatClassPath(path);
		this.classLoader = new WeakReference<ClassLoader>(classLoader != null ? classLoader : getDefaultClassLoader());
	}

    /**
     * Truncate the slash, if it's the first character of class path.
     * @param path class path
     * @return formated class path
     */
    private static String formatClassPath(String path) {
        String p = path.trim();
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        return StringUtils4Resource.cleanPath(p);
    }

    /**
	 * Create a new ClassPathResource for Class usage.
	 * The path can be relative to the given class,
	 * or absolute within the classpath via a leading slash.
	 * @param path relative or absolute path within the class path
	 * @param clazz the class to load resources with
	 * @see java.lang.Class#getResourceAsStream
	 */
	public ClassPathResource(String path, Class clazz) {
		assert (path == null);
		this.path = StringUtils4Resource.cleanPath(path);
		this.clazz = clazz;
	}

	/**
	 * Create a new ClassPathResource with optional ClassLoader and Class.
	 * Only for internal usage.
	 * @param path relative or absolute path within the classpath
	 * @param classLoader the class loader to load the resource with, if any
	 * @param clazz the class to load resources with, if any
	 */
	protected ClassPathResource(String path, ClassLoader classLoader, Class clazz) {
		this.path = path;
		this.classLoader = new WeakReference<ClassLoader>(classLoader);
		this.clazz = clazz;
	}


	/**
	 * Return the path for this resource.
	 */
	public final String getPath() {
		return this.path;
	}

    public final ClassLoader getResourceClassLoader() {
        return classLoader.get();
    }

    /**
	 * This implementation opens an InputStream for the given class path resource.
	 * @see java.lang.ClassLoader#getResourceAsStream(String)
	 * @see java.lang.Class#getResourceAsStream(String)
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else {
			is = this.classLoader.get().getResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}

	/**
	 * This implementation returns a URL for the underlying class path resource.
	 * @see java.lang.ClassLoader#getResource(String)
	 * @see java.lang.Class#getResource(String)
	 */
	public URL getURL() throws IOException {
		URL url;
		if (this.clazz != null) {
			url = this.clazz.getResource(this.path);
		}
		else {
			url = this.classLoader.get().getResource(this.path);
		}
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	public File getFile() throws IOException {
		return getFile(getURL(), getDescription());
	}

	/**
	 * This implementation creates a ClassPathResource, applying the given path
	 * relative to the path of the underlying resource of this descriptor.
	 * @see org.springframework.util.StringUtils#applyRelativePath(String, String)
	 */
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils4Resource.applyRelativePath(this.path, relativePath);
		return new ClassPathResource(pathToUse, this.classLoader.get(), this.clazz);
	}

	/**
	 * This implementation returns the name of the file that this class path
	 * resource refers to.
	 * @see org.springframework.util.StringUtils#getFilename(String)
	 */
	public String getFilename() {
		return StringUtils4Resource.getFilename(this.path);
	}

	/**
	 * This implementation returns a description that includes the class path location.
	 */
	public String getDescription() {
		return "class path resource [" + this.path + "]";
	}

	/**
	 * Return the default ClassLoader to use: typically the thread context
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtilsd
	 * class will be used as fallback.
	 * <p>Call this method if you intend to use the thread context ClassLoader
	 * in a scenario where you absolutely need a non-null ClassLoader reference:
	 * for example, for class path resource loading (but not necessarily for
	 * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
	 * reference as well).
	 * @return the default ClassLoader (never <code>null</code>)
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	private ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			logger.debug("Cannot access thread context ClassLoader - falling back to system class loader", ex);
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = this.getClass().getClassLoader();
		}
		return cl;
	}

	/**
	 * This implementation compares the underlying class path locations.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ClassPathResource) {
			ClassPathResource otherRes = (ClassPathResource) obj;
			return (this.path.equals(otherRes.path) &&
					nullSafeEquals(this.classLoader, otherRes.classLoader) &&
					nullSafeEquals(this.clazz, otherRes.clazz));
		}
		return false;
	}

	/**
	 * This implementation returns the hash code of the underlying
	 * class path location.
	 */
	public int hashCode() {
		return this.path.hashCode();
	}
	
	/**
	 * Determine if the given objects are equal, returning <code>true</code>
	 * if both are <code>null</code> or <code>false</code> if only one is
	 * <code>null</code>.
	 * <p>Compares arrays with <code>Arrays.equals</code>, performing an equality
	 * check based on the array elements rather than the array reference.
	 * @param o1 first Object to compare
	 * @param o2 second Object to compare
	 * @return whether the given objects are equal
	 * @see java.util.Arrays#equals
	 */
	private static boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1.equals(o2)) {
			return true;
		}
		if (o1 instanceof Object[] && o2 instanceof Object[]) {
			return Arrays.equals((Object[]) o1, (Object[]) o2);
		}
		if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
			return Arrays.equals((boolean[]) o1, (boolean[]) o2);
		}
		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return Arrays.equals((byte[]) o1, (byte[]) o2);
		}
		if (o1 instanceof char[] && o2 instanceof char[]) {
			return Arrays.equals((char[]) o1, (char[]) o2);
		}
		if (o1 instanceof double[] && o2 instanceof double[]) {
			return Arrays.equals((double[]) o1, (double[]) o2);
		}
		if (o1 instanceof float[] && o2 instanceof float[]) {
			return Arrays.equals((float[]) o1, (float[]) o2);
		}
		if (o1 instanceof int[] && o2 instanceof int[]) {
			return Arrays.equals((int[]) o1, (int[]) o2);
		}
		if (o1 instanceof long[] && o2 instanceof long[]) {
			return Arrays.equals((long[]) o1, (long[]) o2);
		}
		if (o1 instanceof short[] && o2 instanceof short[]) {
			return Arrays.equals((short[]) o1, (short[]) o2);
		}
		return false;
	}

}
