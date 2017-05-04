package io.jenkins.blueocean.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.VectorSet;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JsAst;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.jscomp.PrintStreamErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap.DetailLevel;
import com.google.javascript.jscomp.SourceMap.Format;
import com.google.javascript.jscomp.VariableRenamingPolicy;
import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.rhino.Node;

import hudson.Extension;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.PluginWrapper.Dependency;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.RootRoutable;
import jenkins.model.Jenkins;
import jenkins.util.AntClassLoader;
import net.sf.json.JSONObject;

@Extension
public class ResourceLoader implements RootRoutable {
	private static final String URL_NAME = "res";

	private static Logger logger = Logger.getLogger(ResourceLoader.class);

	private BlueOceanUI ui = new BlueOceanUI();
	
	abstract static class ResourceFinder<T> {
		abstract String getResourcePattern(PluginWrapper p);
	}

	private Map<String,Resource<String>> sourceMaps = new ConcurrentHashMap<>();
	private Resource<String> script;
	private Resource<String> style;
	private Resource<String> sourceMap;
	private Resource<String> hash;
	
	@Override
	public String getUrlName() {
		return URL_NAME;
	}
	
	/**
	 * Returns the URL for this resource based on the currently installed plugins using a hash
	 */
	public String getJsUrl() throws IOException {
		processJS();
		return Jenkins.getInstance().getRootUrl() + URL_NAME + '/' + hash.getContent() + "/js";
	}

	/**
	 * Stapler-friendly to deal with the hash
	 */
	public ResourceLoader getDynamic(String hash) {
		return this;
	}
	
	public void doCss(StaplerRequest req, StaplerResponse rsp) throws IOException {
		ResourceList<String> cssResources = new ResourceList<>();
		appendResources(cssResources, new ResourceFinder<String>() {
			@Override
			public String getResourcePattern(PluginWrapper p) {
				return "org/jenkins/ui/jsmodules/" + p.getShortName() + "/extensions.css";
			}
		});

		String css = compileCSS(cssResources);
		
		Writer w = rsp.getWriter();
		w.write(css);
		w.close();
	}

	/**
	 * Serves the combined javascript
	 * @param req
	 * @param rsp
	 * @throws IOException
	 * @throws DateParseException 
	 */
	public void doJs(StaplerRequest req, StaplerResponse rsp) throws IOException, DateParseException {
		script = null;
		processJS();

		String ifModifiedSince = req.getHeader("If-Modified-Since");
		if (ifModifiedSince != null && DateUtil.parseDate(ifModifiedSince).getTime() >= script.getLastModified()) {
			rsp.setStatus(StaplerResponse.SC_NOT_MODIFIED);
			return;
		}
		
		// Respond to sourcemap requests
		if (req.getParameter("sourcemap") != null) {
			rsp.setContentType("application/json");
			rsp.setDateHeader("Last-Modified", script.getLastModified());
//	        rsp.setHeader("Cache-Control", "public, max-age=31536000");
			Writer w = rsp.getCompressedWriter(req);
			Resource<String> r = sourceMap;
			if (req.getParameter("name") != null) {
				r = sourceMaps.get(req.getParameter("name"));
			}
			if (r != null) {
				w.write(r.getContent());
			}
			w.close();
			return;
		}
		
		rsp.setContentType("application/x-javascript");
//        rsp.setHeader("Cache-Control", "public, max-age=31536000");
		rsp.setDateHeader("Last-Modified", script.getLastModified());
		rsp.setHeader("X-SourceMap", req.getRequestURI() + "?sourcemap");
		//rsp.setDateHeader("Expires", new Date(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000)).getTime());
		Writer w = rsp.getCompressedWriter(req);
		w.write(script.getContent());
		w.close();
	}

	private void processJS() throws IOException {
		if (script != null &&
				(!ui.isDevelopmentMode()
				|| script.getLastModified() >= script.dependencies.getLastModified())) {
			return;
		}
		
		//		ResourceList<String> extensionJson = listResources(new ResourceFinder<String>() {
		//			@Override
		//			public String getResourcePattern(PluginWrapper p) {
		//				return "jenkins-js-extension.json";
		//			}
		//		});
		
		ResourceList<String> resources = new ResourceList<>();
		PluginWrapper boWeb = Jenkins.getInstance().getPluginManager().getPlugin("blueocean-web");
		ClassLoader loader = boWeb.classLoader;
		resources.add(new Resource<String>(boWeb, "/blueocean.js", loader.getResource("io/jenkins/blueocean/blueocean.js")));
		
		appendResources(resources, new ResourceFinder<String>() {
			@Override
			public String getResourcePattern(PluginWrapper p) {
				return "org/jenkins/ui/jsmodules/" + p.getShortName() + "/jenkins-js-extension.js";
			}
		});
		
		appendResources(resources, new ResourceFinder<String>() {
			@Override
			public String getResourcePattern(PluginWrapper p) {
				return "/blueocean-extension.js";
			}
		});
		
		long lastModified = script == null ? 0 : script.getLastModified();
		
		// Fill out dependencies
		for (Resource<String> r : resources) {
			Resource<String> existing = script == null ? null : script.dependencies.find(r.url);
			if (existing == null || lastModified < existing.getLastModified()) {
				processScriptFile(lastModified, r);
			} else {
				r.dependencies = existing.dependencies;
			}
		}
		
		// just return if nothing modified
		if (lastModified >= resources.getLastModified()) {
			return;
		}
		
		// something modified or first run, compile the script
		
		resources.add(1, collectPluginNames(resources));
		resources.add(2, new Resource<String>(boWeb, "/blueocean-legacy-compat.js", loader.getResource("blueocean-legacy-compat.js")));
		
		//		StringBuilder sb = new StringBuilder();
		//		for (Resource<String> r : extensionJson) {
		//			// TODO temporary workaround that handles stuff
		//			JSONObject o = JSONObject.fromObject(r.getContent());
		//			sb.append(o.toString());
		//			sb.append("\n");
		//		}
		
		Set<Object> alreadyOutput = new HashSet<>();
		
		compileJavascript("/blueocean.js", resources, alreadyOutput);
	}
	
	private Resource<String> collectPluginNames(ResourceList<String> resources) {
		final StringBuilder s = new StringBuilder();
		s.append("var blueOceanResourceNames = [");
		appendAllDependencies(s, resources);
		s.append("];");
		
		return new Resource<String>("/blueOceanDependencyNames", 0, s.toString());
	}
	
	private void appendAllDependencies(StringBuilder s, ResourceList<String> resources) {
		for (Resource<String> r : resources) {
			if (r.plugin == null) {
				s.append("'").append("jenkins-js-module:").append(r.fileName).append(":js',");
			} else {
				appendAllDependencies(s, r.dependencies);
				// 'jenkins-js-module:blueocean-dashboard:jenkins-js-extension:js'
				s.append("'").append("jenkins-js-module:").append(r.plugin.getShortName()).append(":jenkins-js-extension:js',");
			}
		}
	}
	
	private void appendScripts(ResourceList<JsAst> out, Compiler compiler) {
		ResourceFinder<String> finder = new ResourceFinder<String>() {
			@Override
			public String getResourcePattern(PluginWrapper p) {
				return "package.json";
			}
		};
		HashSet<String> includedScripts = new HashSet<>();
 		HashSet<URL> loadedExtensions = new HashSet<>();
		List<PluginWrapper> plugins = getSortedPlugins();
		for (PluginWrapper p : plugins) {
			try {
				List<Resource<String>> scriptFiles = findFiles(p.classLoader, "**/*.js");
				for (Resource<String> s : scriptFiles) {
					System.out.println("Found script file: " + s.fileName);
				}
				Enumeration<URL> urls = p.classLoader.getResources(finder.getResourcePattern(p));
				while (urls.hasMoreElements()) {
					URL u = urls.nextElement();
					if (u != null && !loadedExtensions.contains(u)) {
						loadedExtensions.add(u);
						
						if ("file".equals(u.getProtocol())) {
						}
						
						System.out.println("Found package.json at: " + u.toExternalForm());
						
						Resource<String> r = new Resource<>(p, "package.json", u);
						JSONObject o = JSONObject.fromObject(r.getContent());
						
						String main = null;
						
						try {
							main = o.getString("main");
						} catch(Exception e) {
							// ignore
						}

						if (main != null) {
							System.out.println("Main script at: " + main);
							
							String mainFile = dirname(u.toExternalForm()) + '/' + main;
							r = findFile(u, new URL(mainFile));
							
							if (r != null && r.exists()) {
								String node_modules = dirname(u.toExternalForm());
								// find a node_modules directory somewhere
								while (node_modules.indexOf('/') >= 0) {
									if (new Resource<String>(node_modules + "/node_modules").exists()) {
										node_modules += "/node_modules";
										break;
									} else {
										node_modules = dirname(node_modules);
									}
								}
								appendScriptAndRequires(p.getShortName() + ".js", u, r, out, compiler, includedScripts, node_modules);
							} else {
								System.out.println("Unable to find main file: " + mainFile);
							}
						}
					}
				}
			} catch (IOException e1) {
				logger.warn("Unable to find package.json for plugin: " + p.getShortName(), e1);
			}
		}
	}

	private List<Resource<String>> findFiles(ClassLoader classLoader, String string) throws IOException {
		List<Resource<String>> out = new ArrayList<>();
		try {
			if (classLoader instanceof AntClassLoader) {
				Field f = AntClassLoader.class.getDeclaredField("pathComponents");
				f.setAccessible(true);
				VectorSet pathComponents = (VectorSet)f.get(classLoader);
				System.out.println("Got pathComponents: " + pathComponents);
				if (pathComponents.size() > 0) {
					File pathComponent = new File(pathComponents.get(0).toString());
					if (pathComponent.isDirectory()) {
						// this is an hpi:run
						if (pathComponents.size() > 1) {
							File classesDir = new File(pathComponents.get(1).toString());
							if (classesDir.isDirectory()) {
								return findFilesOnFilesystem(classesDir, string);
							}
						}
						return findFilesOnFilesystem(pathComponent, string);
					} else {
						return findFilesInJar(pathComponent, string);
					}
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	private List<Resource<String>> findFilesInJar(File jarFile, String pathGlob) throws IOException {
		List<Resource<String>> out = new ArrayList<>();
		Pattern matcher = Pattern.compile(
			("\\Q" + pathGlob.replace("**", "\\E\\Q").replace("*", "\\E[^/]*\\Q").replace("\\E\\Q", "\\E.*\\Q") + "\\E").replace("\\Q\\E", "")
		);
		try (JarFile jar = new JarFile(jarFile)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				if (matcher.matcher(entry.getName()).matches()) {
					final String content;
					try {
						content = new String(IOUtils.toByteArray(jar.getInputStream(entry)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					out.add(new Resource<String>(entry.getName(), jarFile.lastModified(), null) {
						String getContent() {
							return content;
						}
					});
				}
			}
		}
		return out;
	}

	private List<Resource<String>> findFilesOnFilesystem(File pathComponent, String pathGlob) {
		List<Resource<String>> out = new ArrayList<>();
		Pattern matcher = Pattern.compile(
			("\\Q" + pathGlob.replace("**", "\\E\\Q").replace("*", "\\E[^/]*\\Q").replace("\\E\\Q", "\\E.*\\Q") + "\\E").replace("\\Q\\E", "")
		);
		collectFilesOnFilesystem(pathComponent, matcher, out);
		return out;
	}

	private void collectFilesOnFilesystem(final File f, Pattern matcher, List<Resource<String>> out) {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				collectFilesOnFilesystem(c, matcher, out);
			}
		} else if (matcher.matcher(f.getAbsolutePath().replace(File.separatorChar, '/')).matches()) {
			out.add(new Resource<String>(f.getName(), f.lastModified(), null) {
				String getContent() {
					try {
						return IOUtils.toString(new FileInputStream(f));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	private void appendScriptAndRequires(String fileName, URL packageJson, Resource<String> r, ResourceList<JsAst> required, Compiler compiler, Set<String> included, String node_modules) throws IOException {
		final JsAst ast = compileScript('/' + r.fileName, r.getContent());
		Node root = ast.getAstRoot(compiler);
		List<String> requires = new ArrayList<>();
		collectRequires(root, requires);
		
		String resourceRoot = dirname(r.url.toExternalForm());
		for (String require : requires) {
			String dir = resourceRoot;
			if (require.startsWith(".")) {
				// relative
				while (require.startsWith("../")) {
					require = require.replaceFirst("../", "");
					dir = dirname(dir);
				}
			}
			else {
				// node_modules
				dir = node_modules;
			}
			
			String file = dir + '/' + require;
			Resource<String> found = findFile(packageJson, new URL(file));
			if (found != null) {
				appendScriptAndRequires(require, packageJson, found, required, compiler, included, node_modules);
			} else {
				System.out.println("Unable to find: " + file);
			}
		}
		
		// After processing all the required stuff, add this script
		required.add(new Resource<JsAst>(fileName, 0, null) {
			JsAst getContent() {
				return ast;
			}
		});
	}
	
	private Resource<String> findFile(URL packageJson, URL url) {
		Resource<String> r = new Resource<String>(url.toExternalForm() + "/package.json");
		if (r.exists()) {
			JSONObject o = JSONObject.fromObject(r.getContent());
			try {
				String main = o.getString("main");
				return new Resource<String>(url.toExternalForm() + "/" + main);
			} catch(Exception e) {
				// no main property
			}
		}
		r = new Resource<String>(url.toExternalForm() + ".js");
		if (r.exists()) {
			return r;
		}
		r = new Resource<String>(url.toExternalForm() + "/index.js");
		if (r.exists()) {
			return r;
		}
		try {
			if (new Resource<String>(null, null, url).getContent() != null) {
				return new Resource<String>(null, url.toExternalForm(), url);
			}
		} catch(Exception e) {
			// not a file!
		}
		return null;
	}

	private String dirname(String s) {
		if (s.endsWith("/")) {
			s = s.substring(0, s.length() - 2);
		}
		return s.replaceFirst("[/][^/]+$", "");
	}

	private void collectRequires(Node node, List<String> requires) {
		if (node.isCall()) {
			Node name = node.getFirstChild();
			if (name != null && name.isName()) {
				if("require".equals(name.getString())) {
					Node file = name.getNext();
					if (file.isString()) {
						requires.add(file.getString());
					}
				}
			}
		}
		if (node.hasChildren()) {
			for (Node child : node.children()) {
				collectRequires(child, requires);
			}
		}
	}

	/**
	 * Appends direct resources from plugins to the provided ResourceList
	 * @param handler
	 * @return
	 * @throws IOException
	 */
	private <T> void appendResources(ResourceList<T> out, ResourceFinder<T> handler) throws IOException {
		HashSet<URL> loadedExtensions = new HashSet<>();
		List<PluginWrapper> plugins = getSortedPlugins();
		for (PluginWrapper p : plugins) {
			URL u = p.classLoader.getResource(handler.getResourcePattern(p));
			if (u != null && !loadedExtensions.contains(u)) {
				loadedExtensions.add(u);
				// TODO figure out how to make relative paths work
				String fileName = u.toExternalForm().replaceAll(".*[/\\\\]", "");
				String resourceName = "/" + p.getShortName();
				if ("file".equals(u.getProtocol())) {
					resourceName += "/" + fileName;
				} else {
					resourceName = "/" + p.getShortName() + ".js";
				}
				Resource<T> r = new Resource<T>(p, resourceName, u);
				out.add(r);
			}
		}
	}

	/**
	 * @param code
	 *            JavaScript source code to compile.
	 * @return The compiled version of the code.
	 * @throws IOException 
	 */
	public void compileJavascript(String fileName, final ResourceList<String> resources, Set<Object> alreadyOutput) throws IOException {
		CompilerOptions options = new CompilerOptions();
		
		List<SourceFile> externs;
		
		boolean compressAndOptimize = !ui.isDevelopmentMode();
		if (compressAndOptimize) {
			options.setVariableRenaming(VariableRenamingPolicy.ALL);

			options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
			options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
	
			//options.setParseJsDocDocumentation(JsDocParsing.);
			// TODO use input source maps
			//options.setInputSourceMaps(inputSourceMaps);
			
			options.setSourceMapOutputPath(fileName);
			options.setSourceMapFormat(Format.DEFAULT);
			options.setSourceMapIncludeSourcesContent(true);
			options.setSourceMapDetailLevel(DetailLevel.ALL);
			
			options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
	
			CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
			
			externs = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
		} else {
			options.setVariableRenaming(VariableRenamingPolicy.OFF);

			options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
			options.setLanguageOut(CompilerOptions.LanguageMode.NO_TRANSPILE);
			
			options.setSourceMapOutputPath(fileName);
			options.setSourceMapFormat(Format.DEFAULT);
			options.setSourceMapIncludeSourcesContent(true);
			options.setSourceMapDetailLevel(DetailLevel.ALL);
			
			CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
			
			options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
			
			externs = Collections.emptyList();
		}

		List<SourceFile> list = new ArrayList<>(); //

		//options.setProcessCommonJSModules(true);
		//options.setModuleRoots(moduleRoots);
		//options.setTweakProcessing(tweakProcessing);
		//options.setModuleResolutionMode(ResolutionMode.NODE);
		
		com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();

		compiler.setErrorManager(new PrintStreamErrorManager(new PrintStream(new ByteArrayOutputStream())) {
			@Override
			public void report(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
				}
			}
			@Override
			public void println(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
				}
			}
		});
		
		compiler.compile(externs, Collections.<SourceFile>emptyList(), options);
		
		PluginWrapper boWeb = Jenkins.getInstance().getPluginManager().getPlugin("blueocean-web");
		ClassLoader loader = boWeb.classLoader;

		JsAst pre = new JsAst(SourceFile.fromCode("/pre", new Resource<String>(null, null,loader.getResource("browserify-compat.js")).getContent()));
		compiler.addNewScript(pre);
		
		ResourceList<JsAst> required = new ResourceList<>();
		appendScripts(required, compiler);
		
		for (Resource<JsAst> r : required) {
			JsAst ast = r.getContent();
			Node root = ast.getAstRoot(compiler);
			// Normalize the file names
			wrapScriptWithExports(r.fileName, compiler, root);
			compiler.addNewScript(ast);
			//compiler.reportCodeChange();
		}
		
//		for (Resource<String> r : resources) {
//			JsAst ast = compileScript(r.fileName, r.getContent());
//			compiler.addNewScript(ast);
//			compiler.reportCodeChange();
//			//appendResource(r, list, alreadyOutput);
//		}
		
		//compiler.compile(externs, list, options);
		
		JsAst post = new JsAst(SourceFile.fromCode("/post", new Resource<String>(null, null, loader.getResource("browserify-post.js")).getContent()));
		compiler.addNewScript(post);

		String scriptText = compiler.toSource();
		long lastCompiled = resources.getLastModified();
		
		StringWriter sw = new StringWriter();
		compiler.getSourceMap().appendTo(sw, fileName);
		String sourceMapText = sw.toString();
		
		// FIXME this is if we're delivering a single file
		sourceMap = new Resource<String>("sourcemap", 0, sourceMapText);
		sourceMaps.put(fileName, sourceMap);
		
		script = new Resource<String>("source", lastCompiled, scriptText, resources);
	}

//	private Node wrapRoot;
//	private final JsAst wrapFunction = compileScript("wrapperFunction", "(function(require,module,exports){'';});");
//	{
//		com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
//		CompilerOptions options = new CompilerOptions();
//		options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
//		options.setLanguageOut(CompilerOptions.LanguageMode.NO_TRANSPILE);
//		Node root = wrapFunction.getAstRoot(compiler);
//		Node insertion = root.getFirstChild().getFirstChild().getFirstChild().getNext().getNext().getFirstChild();
//		insertion.removeChildren();
//		wrapRoot = root.cloneTree();
//	}
	private void wrapScriptWithExports(String fileName, Compiler compiler, Node script) throws IOException {
		JsAst wrapFunction = compileScript("/wrapperFunction", "requires['" + fileName + "']=function(require,module,exports){'asdf';};");
		Node root = wrapFunction.getAstRoot(compiler).cloneTree();
		Node top = root.getFirstChild();
		Node insertion = findStringNode(root, "asdf").getParent().getParent();
		insertion.removeChildren();
		for (Node child : script.children()) {
			insertion.addChildToBack(child.detach());
		}
		script.addChildToFront(top.detach());
		// System.out.println(script.toStringTree());
	}
	
	private Node findStringNode(Node n, String s) {
		if (n.isString() && s.equals(n.getString())) {
			return n;
		}
		if (n.hasChildren()) {
			for (Node c : n.children()) {
				Node found = findStringNode(c, s);
				if (found != null) {
					return found;
				}
			}
		}
		if (n.getNext() != null) {
			return findStringNode(n.getNext(), s);
		}
		return null;
	}

	/**
	 * Appends a resource, preceded by dependencies to the list of source files
	 */
	private void appendResource(Resource<String> r, List<SourceFile> list, Set<Object> processed) {
		// TODO version checks
		if (processed.contains(r.fileName)) {
			return;
		}
		processed.add(r.fileName);
		
		for (Resource<String> dep : r.dependencies) {
			appendResource(dep, list, processed);
		}
		
//		SourceFile preamble = SourceFile.fromCode(r.fileName + "_preamble.js", "\"FILE: " + r.fileName + "\";\n");
//		list.add(preamble);
		
		if (r.getContent() == null) {
			return;
		}
		
		SourceFile source = SourceFile.fromCode(r.fileName, r.getContent());
		list.add(source);
	}

	/**
	 * 
	 */
	private void processScriptFile(long lastModified, Resource<String> r) throws IOException {
		com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
		CompilerOptions options = new CompilerOptions();
		
		options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
		options.setLanguageOut(CompilerOptions.LanguageMode.NO_TRANSPILE);
		
		options.setPrettyPrint(true);
		options.setVariableRenaming(VariableRenamingPolicy.OFF);

		// Report errors here
		compiler.setErrorManager(new PrintStreamErrorManager(new PrintStream(new ByteArrayOutputStream())) {
			@Override
			public void report(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
				}
			}
			@Override
			public void println(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
				}
			}
		});

		// TODO use input source maps
		//options.setInputSourceMaps(inputSourceMaps);

		CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);

		options.setChecksOnly(true);
		options.setContinueAfterErrors(true);
		options.setAllowHotswapReplaceScript(true);
		options.setPreserveDetailedSourceInfo(true);
		options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
		
		List<SourceFile> list = new ArrayList<>();
		
		SourceFile preamble = SourceFile.fromCode(r.fileName + "_preamble.js", "\"FILE: " + r.fileName + "\";\n");
		list.add(preamble);
		
		SourceFile source = SourceFile.fromCode(r.fileName, r.getContent());
		list.add(source);
		
		compiler.compile(new ArrayList<SourceFile>(), list, options);
		
		Node rootNode = compiler.getRoot();
		
		collectRequiredResources(rootNode, r.dependencies);
	}

	private void collectRequiredResources(Node node, ResourceList<String> requiredResources) throws IOException {
		if (node.isLocalResultCall()) {
			Node first = node.getFirstChild();
			if (first.isFunction()) {
				Node name = first.getFirstChild();
				if (name.isName() && "require".equals(name.getString())) {
					System.out.println(node.getSourceFileName() + "@" + node.getLineno());
				}
			}
		}
		if (node.isCall()) {
			//System.out.println(node.getSourceFileName() + "@" + node.getLineno());
		}
		if (node.isCall()) {
			Node name = node.getFirstChild();
			if (name != null && name.isName()) {
				if("require".equals(name.getString())) {
					//System.out.println(node.getSourceFileName() + "@" + node.getLineno());
				}
			}
			Node next = node.getNext();
			if (next != null && next.isString() && "requireModule".equals(next.getString())) {
				Node args = next.getPrevious().getParent().getNext();
				String moduleName = args.getString();
				System.out.println(node.getSourceFileName() + " requires module " + moduleName);
				String[] parts = moduleName.split("[:@]");
				if (parts.length > 2) {
					if (!Pattern.compile("\\d+\\.\\d+\\.\\d+.*").matcher(parts[2]).matches()) {
						// does not have an explicit version
						return;
					}
					
					// "/jenkins/adjuncts/5ad1daf4/org/jenkins/ui/jsmodules/blueocean-js-extensions/blueocean-js-extensions-0-0-1.js"
					//String pluginName = node.getSourceFileName().replace(".js", "");
					String resourcePath = "org/jenkins/ui/jsmodules/" + parts[0] + "/" + parts[1];
					if (!"any".equals(parts[2])) {
						resourcePath += "-" + parts[2].replace('.', '-');
					}
					resourcePath += ".js";
					URL u = Jenkins.getInstance().getPluginManager().uberClassLoader.getResource(resourcePath);
					System.out.println(node.getSourceFileName() + " requires module " + moduleName + (u == null ? " but not found" : " found: " + u.toExternalForm()));
					if (u != null) {
						Resource<String> r = new Resource<>(null, moduleName, u);
						requiredResources.add(r);
					}
				}
			}
		}
		
		if ((node.isString() || node.isName())
			&& "doDependencyExport".equals(node.getString())
			&& !node.getNext().isParamList()) {
			//System.out.println("found dependencyExport" + node.getParent().toStringTree());
			
			Node itss = node.getNext().getNext();
			Node args = node.getNext().getNext().getNext().getFirstChild().getFirstChild();
			String moduleName = itss.getString() + ":" + args.getString();
			String[] parts = moduleName.split("[:@]");
			if (parts.length > 2) {
				if (!Pattern.compile("\\d+\\.\\d+\\.\\d+.*").matcher(parts[2]).matches()) {
					// does not have an explicit version
					return;
				}
				
				System.out.println(node.getSourceFileName() + " already includes module " + moduleName);
				// for now this is a placeholder to write script includes
				// so the resources don't get (re)downloaded
				Resource<String> r = new Resource<String>(moduleName, 0, null);
				requiredResources.add(r);
			} else {
				System.out.println("No verison information for: " + moduleName);
			}
		}
		
		// also check for ___$$$___requiredModuleMappings
		if ((node.isString() || node.isName())
				&& "___$$$___requiredModuleMappings".equals(node.getString())) {
			//System.out.println("found ___$$$___requiredModuleMappings" + node.getParent().toStringTree());
			
			if (node.getFirstChild() == null || !node.getFirstChild().isArrayLit()) {
				return;
			}
			
			for (Node child : node.getFirstChild().children()) {
				String moduleName = child.getString();
				String[] parts = moduleName.split("[:@]");
				if (parts.length > 2) {
					String resource = "org/jenkins/ui/jsmodules/" + parts[0] + "/" + parts[1];
					if (!"any".equals(parts[2])) {
						resource += "-" + parts[2].replace('.', '-');
					}
					resource += ".js";
					URL u = Jenkins.getInstance().getPluginManager().uberClassLoader.getResource(resource);
					System.out.println(node.getSourceFileName() + " requires module " + moduleName + (u == null ? " but not found" : " found: " + u.toExternalForm()));
					if (u != null) {
						Resource<String> r = new Resource<>(null, moduleName, u);
						requiredResources.add(r);
					}
				} else {
					System.out.println("No verison information for: " + moduleName);
				}
			}
		}
		
		if ((node.isString() || node.isName()) && node.getString().contains("15.3.2")) {
			//System.out.println("react!!!! : " + node.getParent().getParent().getParent().toStringTree());
		}
		
		if ((node.isString() || node.isName()) && node.getString().contains("15-3-2")) {
			//System.out.println("react!!!! : " + node.getParent().getParent().getParent().toStringTree());
		}
		
		if (node.hasChildren()) {
			for (Node child : node.children()) {
				collectRequiredResources(child, requiredResources);
			}
		}
//		if (node.getNext() != null) {
//			listRequireCalls(node.getNext());
//		}
	}

	/**
	 * @param code
	 *            JavaScript source code to compile.
	 * @return The compiled version of the code.
	 */
	public String compileCSS(ResourceList<String> resources) {
	    StringBuilder sb = new StringBuilder();
	    for (Resource<String> r : resources) {
	    	sb.append(r.getContent());
	    }
	    return sb.toString();
	}

	List<PluginWrapper> getSortedPlugins() {
		final PluginManager pluginManager = Jenkins.getInstance().getPluginManager();
		List<PluginWrapper> l = pluginManager.getPlugins();
		Collections.sort(l, new Comparator<PluginWrapper>() {
			@Override
			public int compare(PluginWrapper a, PluginWrapper b) {
				if (isADependencyOfB(a, b)) {
					return -1;
				}
				if (isADependencyOfB(b, a)) {
					return 1;
				}
				return a.compareTo(b);
			}

			private boolean isADependencyOfB(PluginWrapper a, PluginWrapper b) {
				for (Dependency dep : b.getDependencies()) {
					if (dep.shortName.equals(a.getShortName())) {
						return true;
					}
					PluginWrapper pw = pluginManager.getPlugin(dep.shortName);
					if (isADependencyOfB(a, pw)) {
						return true;
					}
				}
				return false;
			}
		});
		return l;
	}
	
	private static JsAst compileScript(String fileName, final String script) {
		SourceFile source = SourceFile.fromCode(fileName, script);
		return new JsAst(source);
	}
	
	public static void main(String[] args) throws IOException {
		CompilerOptions options = new CompilerOptions();
		
		List<SourceFile> externs;
		
		String fileName = "main";
		boolean compressAndOptimize = false;
		if (compressAndOptimize) {
			options.setVariableRenaming(VariableRenamingPolicy.ALL);

			options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
			options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
	
			//options.setParseJsDocDocumentation(JsDocParsing.);
			// TODO use input source maps
			//options.setInputSourceMaps(inputSourceMaps);
			
			options.setSourceMapOutputPath(fileName);
			options.setSourceMapFormat(Format.DEFAULT);
			options.setSourceMapIncludeSourcesContent(true);
			options.setSourceMapDetailLevel(DetailLevel.ALL);
			
			options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
	
			CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
			
			externs = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
		} else {
			options.setVariableRenaming(VariableRenamingPolicy.OFF);

			options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
			options.setLanguageOut(CompilerOptions.LanguageMode.NO_TRANSPILE);
			
			options.setSourceMapOutputPath(fileName);
			options.setSourceMapFormat(Format.DEFAULT);
			options.setSourceMapIncludeSourcesContent(true);
			options.setSourceMapDetailLevel(DetailLevel.ALL);
			
			CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
			
			options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
			
			externs = Collections.emptyList();
		}

		List<SourceFile> list = new ArrayList<>();
		
		//options.setProcessCommonJSModules(true);
		//options.setModuleRoots(moduleRoots);
		//options.setTweakProcessing(tweakProcessing);
		//options.setModuleResolutionMode(ResolutionMode.NODE);
		
		com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
		
		final List<JSError> errors = new ArrayList<>();

		compiler.setErrorManager(new PrintStreamErrorManager(new PrintStream(new ByteArrayOutputStream())) {
			@Override
			public void report(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
					errors.add(error);
				}
			}
			@Override
			public void println(CheckLevel level, JSError error) {
				if (CheckLevel.ERROR.equals(level)) {
					super.report(level, error);
					errors.add(error);
				}
			}
		});
		
		externs = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
		compiler.compile(externs, list, options);
		
		JsAst ast1 = compileScript("file2", "class A { }; var otherStuff = 'abcd';");
		Node astRoot = ast1.getAstRoot(compiler);
		
		wrapDumbCalls(compiler, astRoot.getFirstChild());
		wrapEntireScript(compiler, astRoot.getFirstChild());
		
		compiler.addNewScript(ast1);
		compiler.reportCodeChange();
		
		String scriptText = compiler.toSource();
		
		StringWriter sw = new StringWriter();
		compiler.getSourceMap().appendTo(sw, fileName);
		
		String sourceMapText = sw.toString();
		System.out.println("script:\n" + scriptText);
		
		System.out.println("\nsourceMap:\n" + sourceMapText);
		
		main2();
	}
	
	private static void main2() {
		Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setIdeMode(true);
        compiler.initOptions(options);
        Node root = new JsAst(SourceFile.fromCode("asdf", "class Polygon {  constructor(height, width) {}  logWidth() {}  set width(value) {}   get height(value) {} }")).getAstRoot(compiler);
        JavaScriptAnalyzer jsListener = new JavaScriptAnalyzer();
        NodeTraversal.traverseEs6(compiler, root, jsListener);
	}

	public static class JavaScriptAnalyzer extends AbstractShallowCallback {

	    @Override
	    public void visit(NodeTraversal t, Node n, Node parent) {
	        if (n.isClass()) {
	            System.out.println(n.getFirstChild().getString());
	        }
	        if (n.isMemberFunctionDef() || n.isGetterDef() || n.isSetterDef()) {
	            System.out.println(n.getString());
	        }
	    }
	}

	private static void wrapDumbCalls(Compiler compiler, Node node) throws IOException {
		if (node.isClass()) {
			Node[] rootAndReplacement = getRootAndReplacement(compiler);
			rootAndReplacement[1].getParent().replaceChild(rootAndReplacement[1], node.cloneTree());
			node.getParent().replaceChild(node, rootAndReplacement[0].detachFromParent());
			System.out.println(node.toStringTree());
		}
		for (Node child : node.children()) {
			wrapDumbCalls(compiler, child);
		}
	}

	private static Node[] getRootAndReplacement(Compiler compiler) throws IOException {
		JsAst wrapFunction = compileScript("wrap", "doThings($ctor)");
		Node root = wrapFunction.getAstRoot(compiler);
		return new Node[] { root.getFirstChild(), root.getFirstChild().getFirstChild().getFirstChild().getNext() };
	}

	private static void wrapEntireScript(Compiler compiler, Node node) throws IOException {
		Node[] rootAndReplacement = getEntireScriptRootAndReplacement(compiler);
		rootAndReplacement[1].getParent().replaceChild(rootAndReplacement[1], node.cloneTree());
		node.getParent().replaceChild(node, rootAndReplacement[0].detachFromParent());
		System.out.println(node.toStringTree());
	}

	private static Node[] getEntireScriptRootAndReplacement(Compiler compiler) throws IOException {
		JsAst wrapFunction = compileScript("wrapper2", "(function doThings(){'here';})();");
		Node root = wrapFunction.getAstRoot(compiler);
		return new Node[] { root.getFirstChild(), root.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getNext().getNext().getFirstChild() };
	}
}
