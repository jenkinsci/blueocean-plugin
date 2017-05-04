package io.jenkins.blueocean.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.io.IOUtils;

import hudson.PluginWrapper;

/**
 * Holds information to deal with required resources
 */
class Resource<T> {
	String fileName;
	URL url;
	ResourceList<T> dependencies = new ResourceList<>();
	PluginWrapper plugin;
	
	public Resource(String url) {
		try {
			this.fileName = url;
			this.url = new URL(url);
		} catch(Exception e) {
			// oh noes
		}
	}

	public Resource(PluginWrapper plugin, String fileName, URL url) {
		this.fileName = fileName;
		this.url = url;
		this.plugin = plugin;
	}
	
	public Resource(String fileName, final long lastModified, final T content) {
		this.fileName = fileName;
		try {
			this.url = new URL("file", "localhost", 0, fileName, new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					return new URLConnection(u) {
						@Override
						public void connect() throws IOException {
						}
						@Override
						public long getLastModified() {
							return lastModified;
						}
						@Override
						public InputStream getInputStream() throws IOException {
							if (content == null) {
								return null;
							}
							return new ByteArrayInputStream(((String)content).getBytes());
						}
					};
				}
			});
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Resource(String fileName, long lastModified, T content, ResourceList<T> dependencies) {
		this(fileName, lastModified, content);
		this.dependencies = dependencies;
	}
	
	private URLConnection getConnection() {
		try {
			URLConnection conn = url.openConnection();
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			return conn;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	boolean exists() {
		try {
			try (InputStream in = getConnection().getInputStream()) {
				return true;
			}
		} catch(Exception e) {
			return false;
		}
	}
	
	long getLastModified() {
		return getConnection().getLastModified();
	}
	
	@SuppressWarnings("unchecked")
	T getContent() {
		try {
			InputStream in = getConnection().getInputStream();
			if (in == null) {
				return null;
			}
			return (T) new String(IOUtils.toByteArray(in));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return this.fileName;
	}
}
