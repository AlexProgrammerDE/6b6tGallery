package net.pistonmaster.gallery.servlets;

import com.google.common.base.CharMatcher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkArgument;

/*
 * Taken from https://github.com/dirkraft/dropwizard-file-assets
 * and modified to support latest dropwizard.
 */
public class FileAssetServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(FileAssetServlet.class);

    @Serial
    private static final long serialVersionUID = 6393345594784987908L;
    private static final CharMatcher SLASHES = CharMatcher.is('/');
    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;
    private final String resourcePath;
    private final String uriPath;
    private final String indexFile;
    private final Charset defaultCharset;

    /**
     * Creates a new {@code AssetServlet} that serves static assets loaded from {@code resourceURL}
     * (typically a file: or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For
     * example, given a {@code resourceURL} of {@code "file:/data/assets"} and a {@code uriPath} of
     * {@code "/js"}, an {@code AssetServlet} would serve the contents of {@code
     * /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to
     * serve a file with that name in that directory. If a directory is requested and {@code
     * indexFile} is null, it will serve a 404.
     *
     * @param filePath       the base URL from which assets are loaded
     * @param uriPath        the URI path fragment in which all requests are rooted
     * @param indexFile      the filename to use when directories are requested, or null to serve no
     *                       indexes
     * @param defaultCharset the default character set
     */
    public FileAssetServlet(String filePath,
                            String uriPath,
                            String indexFile,
                            Charset defaultCharset) {
        final String trimmedPath = SLASHES.trimFrom(filePath);
        this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + '/';
        final String trimmedUri = SLASHES.trimTrailingFrom(uriPath);
        this.uriPath = trimmedUri.isEmpty() ? "/" : trimmedUri;
        this.indexFile = indexFile;
        this.defaultCharset = defaultCharset;
    }

    public URL getResourceURL() {
        return Resources.getResource(resourcePath);
    }

    public String getUriPath() {
        return uriPath;
    }

    public String getIndexFile() {
        return indexFile;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {
        try {
            final StringBuilder builder = new StringBuilder(req.getServletPath());
            if (req.getPathInfo() != null) {
                builder.append(req.getPathInfo());
            }
            final CachedAsset cachedAsset = loadAsset(builder.toString());
            if (cachedAsset == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (isCachedClientSide(req, cachedAsset)) {
                resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            resp.setDateHeader(HttpHeaders.LAST_MODIFIED, cachedAsset.getLastModifiedTime());
            resp.setHeader(HttpHeaders.ETAG, cachedAsset.getETag());

            final String mimeTypeOfExtension = req.getServletContext()
                    .getMimeType(req.getRequestURI());
            MediaType mediaType = DEFAULT_MEDIA_TYPE;

            if (mimeTypeOfExtension != null) {
                try {
                    mediaType = MediaType.parse(mimeTypeOfExtension);
                    if (defaultCharset != null && mediaType.is(MediaType.ANY_TEXT_TYPE)) {
                        mediaType = mediaType.withCharset(defaultCharset);
                    }
                } catch (IllegalArgumentException ignore) {
                }
            }

            resp.setContentType(mediaType.type() + '/' + mediaType.subtype());

            if (mediaType.charset().isPresent()) {
                resp.setCharacterEncoding(mediaType.charset().get().toString());
            }

            try (ServletOutputStream output = resp.getOutputStream()) {
                output.write(cachedAsset.getResource());
            }
        } catch (RuntimeException | URISyntaxException ignored) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
        checkArgument(key.startsWith(uriPath));
        final String requestedResourcePath = SLASHES.trimFrom(key.substring(uriPath.length()));
        final String combinedRequestedResourcePath = SLASHES.trimFrom(this.resourcePath + requestedResourcePath);

        File requestedResourceFile = new File(combinedRequestedResourcePath);
        if (requestedResourceFile.isDirectory()) {
            if (indexFile != null) {
                requestedResourceFile = requestedResourceFile.toPath().resolve(indexFile).toFile();
            } else {
                // directory requested but no index file defined
                return null;
            }
        }

        long lastModified = requestedResourceFile.lastModified();
        if (lastModified < 1) {
            // Something went wrong trying to get the last modified time: just use the current time
            lastModified = System.currentTimeMillis();
        }

        // zero out the millis since the date we get back from If-Modified-Since will not have them
        lastModified = (lastModified / 1000) * 1000;
        LOG.debug("Attempting to read file {}", requestedResourceFile.getAbsolutePath());

        if (!requestedResourceFile.exists()) {
            // doGet will translate a null CachedAsset return into 404
            return null;
        }
        return new CachedAsset(Files.toByteArray(requestedResourceFile), lastModified);
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
                (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
    }

    private static class CachedAsset {
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource, long lastModifiedTime) {
            this.resource = resource;
            this.eTag = '"' + Hashing.murmur3_128().hashBytes(resource).toString() + '"';
            this.lastModifiedTime = lastModifiedTime;
        }

        public byte[] getResource() {
            return resource;
        }

        public String getETag() {
            return eTag;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }
    }
}
