package org.zeith.cloudflared.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

class HttpRequest implements AutoCloseable {

    private static String getValidCharset(String charset) {
        if (charset != null && !charset.isEmpty()) {
            return charset;
        }
        return "UTF-8";
    }

    private static void addPathSeparator(String baseUrl, StringBuilder result) {
        if (baseUrl.indexOf(':') + 2 == baseUrl.lastIndexOf('/')) result.append('/');
    }

    private static void addParamPrefix(String baseUrl, StringBuilder result) {
        int queryStart = baseUrl.indexOf('?');
        int lastChar = result.length() - 1;
        if (queryStart == -1) {
            result.append('?');
        } else if (queryStart < lastChar && baseUrl.charAt(lastChar) != '&') {
            result.append('&');
        }
    }

    private static void addParam(Object key, Object value, StringBuilder result) {
        if (value != null && value.getClass()
            .isArray()) {
            // noinspection ArraysAsListWithZeroOrOneArgument
            value = Arrays.asList(value);
        }
        if (value instanceof Iterable) {

            Iterator<?> iterator = ((Iterable<?>) value).iterator();
            while (iterator.hasNext()) {

                result.append(key);
                result.append("[]=");
                Object element = iterator.next();
                if (element != null) result.append(element);
                if (iterator.hasNext()) {
                    result.append("&");
                }
            }
        } else {
            result.append(key);
            result.append("=");
            if (value != null) {
                result.append(value);
            }
        }
    }

    public void close() {
        disconnect();
    }

    public interface ConnectionFactory {

        ConnectionFactory DEFAULT = url -> (HttpURLConnection) url.openConnection();

        HttpURLConnection create(URL param1URL) throws IOException;

    }

    private static final ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;

    public static class HttpRequestException extends RuntimeException {

        @SuppressWarnings("MissingSerialAnnotation")
        private static final long serialVersionUID = -1170466989781746231L;

        public HttpRequestException(IOException cause) {
            super(cause);
        }

        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }

    protected static abstract class Operation<V> implements Callable<V> {

        protected abstract V run() throws HttpRequest.HttpRequestException, IOException;

        protected abstract void done() throws IOException;

        public V call() throws HttpRequest.HttpRequestException {
            boolean thrown = false;

            try {
                return run();
            } catch (HttpRequestException e) {

                thrown = true;
                throw e;
            } catch (IOException e) {

                thrown = true;
                throw new HttpRequest.HttpRequestException(e);
            } finally {

                try {
                    done();
                } catch (IOException e) {

                    if (!thrown) {
                        // noinspection ThrowFromFinallyBlock
                        throw new HttpRequest.HttpRequestException(e);
                    }
                }
            }
        }
    }

    protected static abstract class CloseOperation<V> extends Operation<V> {

        private final Closeable closeable;

        private final boolean ignoreCloseExceptions;

        protected CloseOperation(Closeable closeable, boolean ignoreCloseExceptions) {
            this.closeable = closeable;
            this.ignoreCloseExceptions = ignoreCloseExceptions;
        }

        protected void done() throws IOException {
            if (this.closeable instanceof Flushable) ((Flushable) this.closeable).flush();
            if (this.ignoreCloseExceptions) {

                try {
                    this.closeable.close();
                } catch (IOException ignored) {}

            } else {

                this.closeable.close();
            }
        }
    }

    public static class RequestOutputStream extends BufferedOutputStream {

        public RequestOutputStream(OutputStream stream, String charset, int bufferSize) {
            super(stream, bufferSize);

            Charset.forName(HttpRequest.getValidCharset(charset))
                .newEncoder();
        }

    }

    public static String encode(CharSequence url) throws HttpRequestException {
        URL parsed;
        try {
            parsed = new URL(url.toString());
        } catch (IOException e) {

            throw new HttpRequestException(e);
        }

        String host = parsed.getHost();
        int port = parsed.getPort();
        if (port != -1) {
            host = host + ':' + port;
        }

        try {
            String encoded = (new URI(parsed.getProtocol(), host, parsed.getPath(), parsed.getQuery(), null))
                .toASCIIString();
            int paramsStart = encoded.indexOf('?');
            if (paramsStart > 0 && paramsStart + 1 < encoded.length())
                encoded = encoded.substring(0, paramsStart + 1) + encoded.substring(paramsStart + 1)
                    .replace("+", "%2B");
            return encoded;
        } catch (URISyntaxException e) {

            IOException io = new IOException("Parsing URI failed", e);
            throw new HttpRequestException(io);
        }
    }

    public static String append(CharSequence url, Map<?, ?> params) {
        String baseUrl = url.toString();
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        Iterator<?> iterator = params.entrySet()
            .iterator();
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iterator.next();
        addParam(
            entry.getKey()
                .toString(),
            entry.getValue(),
            result);

        while (iterator.hasNext()) {

            result.append('&');
            entry = (Map.Entry<?, ?>) iterator.next();
            addParam(
                entry.getKey()
                    .toString(),
                entry.getValue(),
                result);
        }

        return result.toString();
    }

    public static String append(CharSequence url, Object... params) {
        String baseUrl = url.toString();
        if (params == null || params.length == 0) {
            return baseUrl;
        }
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Must specify an even number of parameter names/values");
        }
        StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        addParam(params[0], params[1], result);

        for (int i = 2; i < params.length; i += 2) {

            result.append('&');
            addParam(params[i], params[i + 1], result);
        }

        return result.toString();
    }

    public static HttpRequest get(CharSequence url) throws HttpRequestException {
        return new HttpRequest(url, "GET");
    }

    public static HttpRequest get(URL url) throws HttpRequestException {
        return new HttpRequest(url, "GET");
    }

    public static HttpRequest get(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    public static HttpRequest get(CharSequence baseUrl, boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    private HttpURLConnection connection = null;

    private final URL url;

    private final String requestMethod;

    private RequestOutputStream output;

    private final boolean ignoreCloseExceptions = true;

    private final int bufferSize = 8192;

    private long totalSize = -1L;

    private long totalWritten = 0L;

    private UploadProgress progress = UploadProgress.DEFAULT;

    public HttpRequest(CharSequence url, String method) throws HttpRequestException {
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {

            throw new HttpRequestException(e);
        }
        this.requestMethod = method;
    }

    public HttpRequest(URL url, String method) throws HttpRequestException {
        this.url = url;
        this.requestMethod = method;
    }

    private HttpURLConnection createConnection() {
        try {
            HttpURLConnection connection;
            connection = CONNECTION_FACTORY.create(this.url);
            connection.setRequestMethod(this.requestMethod);
            return connection;
        } catch (IOException e) {

            throw new HttpRequestException(e);
        }
    }

    public String toString() {
        return method() + ' ' + url();
    }

    public HttpURLConnection getConnection() {
        if (this.connection == null) this.connection = createConnection();
        return this.connection;
    }

    public int code() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {

            throw new HttpRequestException(e);
        }
    }

    public void disconnect() {
        getConnection().disconnect();
    }

    public BufferedInputStream buffer() throws HttpRequestException {
        return new BufferedInputStream(stream(), this.bufferSize);
    }

    public InputStream stream() throws HttpRequestException {
        InputStream stream;
        if (code() < 400) {

            try {
                stream = getConnection().getInputStream();
            } catch (IOException e) {

                throw new HttpRequestException(e);
            }
        } else {

            stream = getConnection().getErrorStream();
            if (stream == null) {

                try {
                    stream = getConnection().getInputStream();
                } catch (IOException e) {

                    if (contentLength() > 0) {
                        throw new HttpRequestException(e);
                    }
                    stream = new ByteArrayInputStream(new byte[0]);
                }
            }
        }
        return stream;

    }

    public void receive(File file) throws HttpRequestException {
        final OutputStream output;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file), this.bufferSize);
        } catch (FileNotFoundException e) {

            throw new HttpRequestException(e);
        }
        (new CloseOperation<HttpRequest>(output, this.ignoreCloseExceptions) {

            protected HttpRequest run() throws HttpRequestException {
                return HttpRequest.this.receive(output);
            }
        }).call();
    }

    public HttpRequest receive(OutputStream output) throws HttpRequestException {
        try {
            return copy(buffer(), output);
        } catch (IOException e) {

            throw new HttpRequestException(e);
        }
    }

    public HttpRequest header(String name, String value) {
        getConnection().setRequestProperty(name, value);
        return this;
    }

    public int intHeader(String name) throws HttpRequestException {
        return intHeader(name, -1);
    }

    public int intHeader(String name, int defaultValue) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }

    public HttpRequest userAgent(String userAgent) {
        return header("User-Agent", userAgent);
    }

    public int contentLength() {
        return intHeader("Content-Length");
    }

    protected HttpRequest copy(final InputStream input, final OutputStream output) throws IOException {
        return (new CloseOperation<HttpRequest>(input, this.ignoreCloseExceptions) {

            public HttpRequest run() throws IOException {
                byte[] buffer = new byte[HttpRequest.this.bufferSize];
                int read;
                while ((read = input.read(buffer)) != -1) {

                    output.write(buffer, 0, read);
                    HttpRequest.this.totalWritten = HttpRequest.this.totalWritten + read;
                    HttpRequest.this.progress.onUpload(HttpRequest.this.totalWritten, HttpRequest.this.totalSize);
                }
                return HttpRequest.this;
            }
        }).call();
    }

    public HttpRequest progress(UploadProgress callback) {
        if (this.progress == null) {
            this.progress = UploadProgress.DEFAULT;
        } else {
            this.progress = callback;
        }
        return this;
    }

    public HttpRequest incrementTotalSize(long size) {
        if (this.totalSize == -1L) this.totalSize = 0L;
        this.totalSize += size;
        return this;
    }

    protected void closeOutput() throws IOException {
        if (this.output == null) return;
        progress(null);
        if (this.ignoreCloseExceptions) {

            try {
                this.output.close();
            } catch (IOException ignored) {}

        } else {

            this.output.close();
        }
        this.output = null;
    }

    protected void closeOutputQuietly() throws HttpRequestException {
        try {
            closeOutput();
        } catch (IOException e) {

            throw new HttpRequestException(e);
        }
    }

    public URL url() {
        return getConnection().getURL();
    }

    public String method() {
        return getConnection().getRequestMethod();
    }
}
