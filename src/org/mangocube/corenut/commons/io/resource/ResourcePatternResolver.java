package org.mangocube.corenut.commons.io.resource;

import org.mangocube.corenut.commons.util.AntPathMatcher;
import org.mangocube.corenut.commons.util.ClassLoaderResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.ref.WeakReference;

/**
 * A {@link ResourcePatternResolver} implementation that is able to resolve a
 * specified resource location path into one or more matching Resources.
 * The source path may be a simple path which has a one-to-one mapping to a
 * target {@link org.springframework.core.io.Resource}, or alternatively
 * may contain the special "<code>classpath*:</code>" prefix and/or
 * internal Ant-style regular expressions (matched using Spring's
 * {@link org.springframework.util.AntPathMatcher} utility).
 * Both of the latter are effectively wildcards.
 * <p/>
 * <p><b>No Wildcards:</b>
 * <p/>
 * <p>In the simple case, if the specified location path does not start with the
 * <code>"classpath*:</code>" prefix, and does not contain a PathMatcher pattern,
 * this resolver will simply return a single resource via a
 * <code>getResource()</code> call on the underlying <code>ResourceLoader</code>.
 * Examples are real URLs such as "<code>file:C:/context.xml</code>", pseudo-URLs
 * such as "<code>classpath:/context.xml</code>", and simple unprefixed paths
 * such as "<code>/WEB-INF/context.xml</code>". The latter will resolve in a
 * fashion specific to the underlaying <code>ResourceLoader</code> (e.g.
 * <code>ServletContextResource</code> for a <code>WebApplicationContext</code>).
 * <p/>
 * <p><b>Ant-style Patterns:</b>
 * <p/>
 * <p>When the path location contains an Ant-style pattern, e.g.:
 * <pre>
 * /WEB-INF/*-context.xml
 * com/mycompany/**&#47;applicationContext.xml
 * file:C:/some/path/*-context.xml
 * classpath:com/mycompany/**&#47;applicationContext.xml</pre>
 * the resolver follows a more complex but defined procedure to try to resolve
 * the wildcard. It produces a <code>Resource</code> for the path up to the last
 * non-wildcard segment and obtains a <code>URL</code> from it. If this URL is
 * not a "<code>jar:</code>" URL or container-specific variant (e.g.
 * "<code>zip:</code>" in WebLogic, "<code>wsjar</code>" in WebSphere", etc.),
 * then a <code>java.io.File</code> is obtained from it, and used to resolve the
 * wildcard by walking the filesystem. In the case of a jar URL, the resolver
 * either gets a <code>java.net.JarURLConnection</code> from it, or manually parse
 * the jar URL, and then traverse the contents of the jar file, to resolve the
 * wildcards.
 * <p/>
 * <p><b>Implications on portability:</b>
 * <p/>
 * <p>If the specified path is already a file URL (either explicitly, or
 * implicitly because the base <code>ResourceLoader</code> is a filesystem one,
 * then wildcarding is guaranteed to work in a completely poratable fashion.
 * <p/>
 * <p>If the specified path is a classpath location, then the resolver must
 * obtain the last non-wildcard path segment URL via a
 * <code>Classloader.getResource()</code> call. Since this is just a
 * node of the path (not the file at the end) it is actually undefined
 * (in the ClassLoader Javadocs) exactly what sort of a URL is returned in
 * this case. In practice, it is usually a <code>java.io.File</code> representing
 * the directory, where the classpath resource resolves to a filesystem
 * location, or a jar URL of some sort, where the classpath resource resolves
 * to a jar location. Still, there is a portability concern on this operation.
 * <p/>
 * <p>If a jar URL is obtained for the last non-wildcard segment, the resolver
 * must be able to get a <code>java.net.JarURLConnection</code> from it, or
 * manually parse the jar URL, to be able to walk the contents of the jar,
 * and resolve the wildcard. This will work in most environments, but will
 * fail in others, and it is strongly recommended that the wildcard
 * resolution of resources coming from jars be thoroughly tested in your
 * specific environment before you rely on it.
 * <p/>
 * <p><b><code>classpath*:</code> Prefix:</b>
 * <p/>
 * <p>There is special support for retrieving multiple class path resources with
 * the same name, via the "<code>classpath*:</code>" prefix. For example,
 * "<code>classpath*:META-INF/beans.xml</code>" will find all "beans.xml"
 * files in the class path, be it in "classes" directories or in JAR files.
 * This is particularly useful for autodetecting config files of the same name
 * at the same location within each jar file. Internally, this happens via a
 * <code>ClassLoader.getResources()</code> call, and is completely portable.
 * <p/>
 * <p>The "classpath*:" prefix can also be combined with a PathMatcher pattern in
 * the rest of the location path, for example "classpath*:META-INF/*-beans.xml".
 * In this case, the resolution strategy is fairly simple: a
 * <code>ClassLoader.getResources()</code> call is used on the last non-wildcard
 * path segment to get all the matching resources in the class loader hierarchy,
 * and then off each resource the same PathMatcher resoltion strategy described
 * above is used for the wildcard subpath.
 * <p/>
 * <p><b>Other notes:</b>
 * <p/>
 * <p><b>WARNING:</b> Note that "<code>classpath*:</code>" when combined with
 * Ant-style patterns will only work reliably with at least one root directory
 * before the pattern starts, unless the actual target files reside in the file
 * system. This means that a pattern like "<code>classpath*:*.xml</code>" will
 * <i>not</i> retrieve files from the root of jar files but rather only from the
 * root of expanded directories. This originates from a limitation in the JDK's
 * <code>ClassLoader.getResources()</code> method which only returns file system
 * locations for a passed-in empty String (indicating potential roots to search).
 * <p/>
 * <p><b>WARNING:</b> Ant-style patterns with "classpath:" resources are not
 * guaranteed to find matching resources if the root package to search is available
 * in multiple class path locations. This is because a resource such as<pre>
 *     com/mycompany/package1/service-context.xml
 * </pre>may be in only one location, but when a path such as<pre>
 *     classpath:com/mycompany/**&#47;service-context.xml
 * </pre>is used to try to resolve it, the resolver will work off the (first) URL
 * returned by <code>getResource("com/mycompany");</code>. If this base package
 * node exists in multiple classloader locations, the actual end resource may
 * not be underneath. Therefore, preferably, use "<code>classpath*:<code>" with the same
 * Ant-style pattern in such a case, which will search <i>all</i> class path
 * locations that contain the root package.
 *
 * @since 1.0
 */

@SuppressWarnings("unchecked")
public class ResourcePatternResolver {

    public static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    /**
     * URL protocol for a file in the file system: "file"
     */
    public static final String URL_PROTOCOL_FILE = "file";

    /**
     * URL protocol for an entry from a jar file: "jar"
     */
    private static final String URL_PROTOCOL_JAR = "jar";

    /**
     * URL protocol for an entry from a zip file: "zip"
     */
    private static final String URL_PROTOCOL_ZIP = "zip";

    /**
     * URL protocol for an entry from a WebSphere jar file: "wsjar"
     */
    private static final String URL_PROTOCOL_WSJAR = "wsjar";

    /**
     * Separator between JAR URL and file path within the JAR
     */
    private static final String JAR_URL_SEPARATOR = "!/";

    private static final String FOLDER_SEPARATOR = "/";

    /**
     * URL prefix for loading from the file system: "file:"
     */
    private static final String FILE_URL_PREFIX = "file:";

    private AntPathMatcher pathMatcher;

    private WeakReference<ClassLoader> classLoader;
    private List<URL> allRootUrlsClassloader;

    protected final Log logger = LogFactory.getLog(getClass());

    private ResourcePatternResolver() {
        pathMatcher = new AntPathMatcher();
        this.classLoader = new WeakReference<ClassLoader>(ClassLoaderResolver.getClassLoader());
    }

    private ResourcePatternResolver(AntPathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
        this.classLoader = new WeakReference<ClassLoader>(ClassLoaderResolver.getClassLoader());
    }

    private ResourcePatternResolver(AntPathMatcher pathMatcher, ClassLoader classLoader) {
        this.pathMatcher = pathMatcher == null ? new AntPathMatcher() : pathMatcher;
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
    }

    private ClassLoader getClassLoader() {
        return classLoader.get();
    }

    /**
     * @return get instance of ResourcePatternResolver, the singleton pattern
     */
    public static ResourcePatternResolver getInstance() {
        return new ResourcePatternResolver();
    }

    public static ResourcePatternResolver getInstance(AntPathMatcher pathMatcher) {
        return new ResourcePatternResolver(pathMatcher);
    }

    public static ResourcePatternResolver getInstance(AntPathMatcher pathMatcher, ClassLoader classLoader) {
        return new ResourcePatternResolver(pathMatcher, classLoader);
    }

    public Resource[] getResources(String locationPattern) throws IOException {
        Resource[] res_list = retrieveResources(locationPattern);
        Resource[] cp_res_list = new Resource[res_list.length];
        System.arraycopy(res_list, 0, cp_res_list, 0, res_list.length);

        for (int i = 0; i < cp_res_list.length; i++) {
            Resource res = cp_res_list[i];
            if (!(res instanceof ClassPathResource)) {
                res = convertClassLoaderURL(res.getURL());
                if (res != null) {
                    cp_res_list[i] = res;
                }
            }
        }

        /*After replacing the resource with ClassPathResource whenever possible, will meet one problem. If classloader
        * contains multiple files with the same path, then such replacement will make them confusion. */
        Map<String, Integer> cp_paths = new HashMap<String, Integer>();
        for (int i = 0; i < cp_res_list.length; i++) {
            Resource res = cp_res_list[i];
            if (!(res instanceof ClassPathResource)) continue;

            ClassPathResource cp_res = (ClassPathResource) res;
            if (cp_paths.containsKey(cp_res.getPath())) {//duplicated path found
                cp_res_list[i] = res_list[i];
                int ridx = cp_paths.get(cp_res.getPath());
                cp_res_list[ridx] = res_list[ridx];
            } else {
                cp_paths.put(cp_res.getPath(), i);
            }
        }

        return cp_res_list;
    }

    protected List<URL> enumerateLoaderUrls() throws IOException {
        if (allRootUrlsClassloader != null) {
            return allRootUrlsClassloader;
        }

        List<URL> res_list = new ArrayList<URL>();
        ClassLoader loader = getClassLoader();
        while (loader instanceof URLClassLoader) {
            URLClassLoader cl = (URLClassLoader) loader;
            URL[] urls = cl.getURLs();

            for (URL url : urls) {
                String ext_file = getJarURLExt(url);
                File f = AbstractResource.getFile(url, url.toString());
                if (!"".equals(ext_file) && !f.isDirectory()) {//Jar file detected
                    url = new URL("jar:" + url.toString() + "!/");
                }
                res_list.add(url);
            }
            loader = loader.getParent();
        }
        allRootUrlsClassloader = res_list;
        return res_list;
    }

    /**
     * Convert the given URL as returned from the ClassLoader into a Resource object.
     *
     * @param url a URL as returned from the ClassLoader
     * @return the corresponding Resource object
     * @see java.lang.ClassLoader#getResources
     */
    protected Resource convertClassLoaderURL(URL url) {
        try {
            List<URL> root_urls = enumerateLoaderUrls();
            String absolute_path = trunkSlash(url);
            for (URL root_url : root_urls) {
                String root_path = trunkSlash(root_url);

                if (url.getProtocol().equals(root_url.getProtocol()) &&
                        absolute_path.length() != root_path.length() && absolute_path.indexOf(root_path) == 0 &&
                        absolute_path.charAt(root_path.length() - 1) == '/') {//it's the sub file of root
                    return new ClassPathResource(absolute_path.substring(root_path.length() - 1), getClassLoader());
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String trunkSlash(URL url) {
        String path = url.getPath();
        if (path.startsWith("/")) {
            return path.substring(1, path.length());
        }
        return path;
    }

    public Resource[] retrieveResources(String locationPattern) throws IOException {
        assert (locationPattern == null);
        if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
            // a class path resource (multiple resources for same name possible)
            if (pathMatcher.isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
                // a class path resource pattern
                return findPathMatchingResources(locationPattern);
            } else {
                // all class path resources with the given name
                return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
            }
        } else {
            // Only look for a pattern after a prefix here
            // (to not get fooled by a pattern symbol in a strange prefix).
            int prefixEnd = locationPattern.indexOf(":") + 1;
            if (pathMatcher.isPattern(locationPattern.substring(prefixEnd))) {
                // a file pattern
                return findPathMatchingResources(locationPattern);
            } else {
                // a single resource with the given name
                return new Resource[]{getResource(locationPattern)};
            }
        }
    }

    public Resource getResource(String location) {
        assert (location == null);
        if (location.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_ALL_URL_PREFIX.length()),
                    getClassLoader() == null ? getDefaultClassLoader() : getClassLoader());
        } else {
            try {
                // Try to parse the location as a URL...
                URL url = new URL(location);
                return new UrlResource(url);
            }
            catch (MalformedURLException ex) {
                // No URL -> resolve as resource path.
                return new ClassPathResource(location, getClassLoader() == null ? getDefaultClassLoader() : getClassLoader());
            }
        }
    }

    /**
     * Truncate the slash, if it's the first character of class path.
     *
     * @param path class path
     * @return formated class path
     */
    private static String formatClassPath(String path) {
        String p = path.trim();
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p;
    }

    /**
     * Find all class location resources with the given location via the ClassLoader.
     *
     * @param location the absolute path within the classpath
     * @return the result as Resource array
     * @throws IOException in case of I/O errors
     * @see java.lang.ClassLoader#getResources
     * @see #convertClassLoaderURL
     */
    protected Resource[] findAllClassPathResources(String location) throws IOException {
        String path = formatClassPath(location);
        ClassLoader loader = getClassLoader();

        if (loader instanceof URLClassLoader && "".equals(path)) {
            //The path is "" which means the root folder of all classloader resources. In such case,
            // classLoader.getResources(path) won't work. But merely get all URLS from URLClassLoader.
            List<URL> urls = enumerateLoaderUrls();
            Resource[] res = new Resource[urls.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = new UrlResource(urls.get(i));
            }
            return res;
        } else {
            Enumeration resourceUrls = null;
            if (loader != null) {
                resourceUrls = loader.getResources(path);
            }
            if (resourceUrls == null || !resourceUrls.hasMoreElements()) {
                resourceUrls = getDefaultClassLoader().getResources(path);
            }
            if (!resourceUrls.hasMoreElements()) {
                resourceUrls = this.getClass().getClassLoader().getResources(path);
            }
            Set result = new LinkedHashSet(16);
            while (resourceUrls.hasMoreElements()) {
                URL url = (URL) resourceUrls.nextElement();
                result.add(new UrlResource(url));
            }
            return (Resource[]) result.toArray(new Resource[result.size()]);
        }
    }

    /**
     * Find all resources that match the given location pattern via the
     * Ant-style PathMatcher. Supports resources in jar files and zip files
     * and in the file system.
     *
     * @param locationPattern the location pattern to match
     * @return the result as Resource array
     * @throws IOException in case of I/O errors
     * @see #doFindPathMatchingJarResources
     * @see #doFindPathMatchingFileResources
     */
    protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        Resource[] rootDirResources = retrieveResources(rootDirPath);
        Set result = new LinkedHashSet(16);
        for (Resource rootDirResource : rootDirResources) {
            if (isJarResource(rootDirResource)) {
                result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
            } else {
                result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resolved location pattern [" + locationPattern + "] to resources " + result);
        }
        return (Resource[]) result.toArray(new Resource[result.size()]);
    }

    /**
     * Determine the root directory for the given location.
     * <p>Used for determining the starting point for file matching,
     * resolving the root directory location to a <code>java.io.File</code>
     * and passing it into <code>retrieveMatchingFiles</code>, with the
     * remainder of the location as pattern.
     * <p>Will return "/WEB-INF" for the pattern "/WEB-INF/*.xml",
     * for example.
     *
     * @param location the location to check
     * @return the part of the location that denotes the root directory
     * @see #retrieveMatchingFiles
     */
    protected String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && pathMatcher.isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    /**
     * Return whether the given resource handle indicates a jar resource
     * that the <code>doFindPathMatchingJarResources</code> method can handle.
     * <p>The default implementation checks against the URL protocols
     * "jar", "zip" and "wsjar" (the latter are used by BEA WebLogic Server
     * and IBM WebSphere, respectively, but can be treated like jar files).
     *
     * @param resource the resource handle to check
     *                 (usually the root directory to start path matching from)
     * @see #doFindPathMatchingJarResources
     */
    protected boolean isJarResource(Resource resource) throws IOException {
        return isJarURL(resource.getURL());
    }

    /**
     * Determine whether the given URL points to a resource in a jar file,
     * that is, has protocol "jar", "zip" or "wsjar".
     * <p>"zip" and "wsjar" are used by BEA WebLogic Server and IBM WebSphere,
     * respectively, but can be treated like jar files.
     *
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR URL
     */
    private static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) ||
                URL_PROTOCOL_ZIP.equals(protocol) ||
                URL_PROTOCOL_WSJAR.equals(protocol));
    }

    private static String getJarURLExt(URL url) {
        String path = url.getPath();
        int last_dot_idx = path.lastIndexOf('.');
        String file_ext = last_dot_idx == -1 ? "" : path.substring(last_dot_idx + 1);

        return (URL_PROTOCOL_JAR.equals(file_ext) || URL_PROTOCOL_ZIP.equals(file_ext) ||
                URL_PROTOCOL_WSJAR.equals(file_ext)) ? file_ext : "";
    }

    /**
     * Find all resources in jar files that match the given location pattern
     * via the Ant-style PathMatcher.
     *
     * @param rootDirResource the root directory as Resource
     * @param subPattern      the sub pattern to match (below the root directory)
     * @return the Set of matching Resource instances
     * @throws IOException in case of I/O errors
     * @see java.net.JarURLConnection
     */
    protected Set doFindPathMatchingJarResources(Resource rootDirResource, String subPattern) throws IOException {
        URLConnection con = rootDirResource.getURL().openConnection();
        JarFile jarFile = null;
        String jarFileUrl = null;
        String rootEntryPath = null;

        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootDirResource.getURL().getFile();
            int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
            jarFileUrl = urlFile.substring(0, separatorIndex);
            if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
                jarFileUrl = jarFileUrl.substring(FILE_URL_PREFIX.length());
            }
            jarFile = new JarFile(jarFileUrl);
            jarFileUrl = FILE_URL_PREFIX + jarFileUrl;
            rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
        }
        if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
            // Root entry path must end with slash to allow for proper matching.
            // The Sun JRE does not return a slash here, but BEA JRockit does.
            rootEntryPath = rootEntryPath + "/";
        }
        Set result = new LinkedHashSet(8);
        for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryPath = entry.getName();
            if (entryPath.startsWith(rootEntryPath)) {
                String relativePath = entryPath.substring(rootEntryPath.length());
                if (pathMatcher.match(subPattern, relativePath)) {
                    result.add(rootDirResource.createRelative(relativePath));
                }
            }
        }
        return result;
    }

    /**
     * Find all resources in the file system that match the given location pattern
     * via the Ant-style PathMatcher.
     *
     * @param rootDirResource the root directory as Resource
     * @param subPattern      the sub pattern to match (below the root directory)
     * @return the Set of matching Resource instances
     * @throws IOException in case of I/O errors
     * @see #retrieveMatchingFiles
     */
    protected Set doFindPathMatchingFileResources(Resource rootDirResource, String subPattern) throws IOException {
        File rootDir = null;
        try {
            rootDir = rootDirResource.getFile().getAbsoluteFile();
        }
        catch (IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot search for matching files underneath " + rootDirResource +
                        " because it does not correspond to a directory in the file system", ex);
            }
            return Collections.EMPTY_SET;
        }
        return doFindMatchingFileSystemResources(rootDir, subPattern);
    }

    /**
     * Find all resources in the file system that match the given location pattern
     * via the Ant-style PathMatcher.
     *
     * @param rootDir    the root directory in the file system
     * @param subPattern the sub pattern to match (below the root directory)
     * @return the Set of matching Resource instances
     * @throws IOException in case of I/O errors
     * @see #retrieveMatchingFiles
     */
    protected Set doFindMatchingFileSystemResources(File rootDir, String subPattern) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
        }
        Set matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
        Set result = new LinkedHashSet(8);
        for (Object matchingFile : matchingFiles) {
            File file = (File) matchingFile;
            result.add(new FileSystemResource(file));
        }
        return result;
    }

    /**
     * Retrieve files that match the given path pattern,
     * checking the given directory and its subdirectories.
     *
     * @param rootDir the directory to start from
     * @param pattern the pattern to match against,
     *                relative to the root directory
     * @return the Set of matching File instances
     * @throws IOException if directory contents could not be retrieved
     */
    protected Set retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Resource path [" + rootDir + "] does not denote a directory");
        }
        String fullPattern = StringUtils4Resource.replace(rootDir.getAbsolutePath(), File.separator, "/");
        if (!pattern.startsWith("/")) {
            fullPattern += "/";
        }
        fullPattern = fullPattern + StringUtils4Resource.replace(pattern, File.separator, "/");
        Set result = new LinkedHashSet(8);
        doRetrieveMatchingFiles(fullPattern, rootDir, result);
        return result;
    }

    /**
     * Recursively retrieve files that match the given pattern,
     * adding them to the given result list.
     *
     * @param fullPattern the pattern to match against,
     *                    with preprended root directory path
     * @param dir         the current directory
     * @param result      the Set of matching File instances to add to
     * @throws IOException if directory contents could not be retrieved
     */
    protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set result) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching directory [" + dir.getAbsolutePath() +
                    "] for files matching pattern [" + fullPattern + "]");
        }
        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            throw new IOException("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
        }
        boolean dirDepthNotFixed = (fullPattern.indexOf("**") != -1);
        for (File dirContent : dirContents) {
            String currPath = StringUtils4Resource.replace(dirContent.getAbsolutePath(), File.separator, "/");
            if (dirContent.isDirectory() &&
                    (dirDepthNotFixed ||
                            StringUtils4Resource.countOccurrencesOf(currPath, "/") < StringUtils4Resource.countOccurrencesOf(fullPattern, "/"))) {
                doRetrieveMatchingFiles(fullPattern, dirContent, result);
            }
            if (pathMatcher.match(fullPattern, currPath)) {
                result.add(dirContent);
            }
        }
    }

    /**
     * This implementation creates a ClassPathResource, applying the given path
     * relative to the path of the underlying resource of this descriptor.
     */
    public File createRelative(File rootDir, String relativePath) {
        if (relativePath == null || relativePath.trim().length() == 0) return rootDir;

        relativePath = relativePath.replace('\\', '/');

        File rf = rootDir;
        for (String p : relativePath.split("/")) {
            if (p == null || p.trim().length() == 0) continue;
            p = p.trim();

            if (".".equals(p) && rf == rootDir) continue;

            if ("..".equals(p)) {//the parent folder
                rf = rf.getParentFile() != null ? rf.getParentFile() : rf;
            } else {
                rf = new File(rf, p);
            }
        }
        return rf;
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
     *
     * @return the default ClassLoader (never <code>null</code>)
     */
    protected ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            logger.debug("Cannot access thread context ClassLoader - falling back to system class loader", ex);
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = getClass().getClassLoader();
        }
        return cl;
    }
}
