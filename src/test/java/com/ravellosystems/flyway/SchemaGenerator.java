package com.ravellosystems.flyway;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.sql.DataSource;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// based on http://jandrewthompson.blogspot.co.il/2009/10/how-to-generate-ddl-scripts-from.html

public class SchemaGenerator {

	private final Configuration cfg;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public SchemaGenerator(Class<? extends Dialect> dialectClass, Class<?> driverClass, String dbUrl,
			String dbUsername, String dbPassword, Set<String> packageNames) throws Exception {

		cfg = new Configuration();
		// cfg.setProperty("hibernate.hbm2ddl.auto", ddlAutoMode);
		cfg.setProperty("hibernate.dialect", dialectClass.getName());
		cfg.setProperty("hibernate.connection.driver_class", driverClass.getName());

		cfg.setProperty("hibernate.connection.url", dbUrl);
		cfg.setProperty("hibernate.connection.username", dbUsername);
		cfg.setProperty("hibernate.connection.password", dbPassword);

		cfg.setNamingStrategy(new org.hibernate.cfg.ImprovedNamingStrategy());

		for (String packageName : packageNames) {
			for (Class<Object> clazz : getClasses(packageName)) {
				cfg.addAnnotatedClass(clazz);
			}
		}
	}

	public void validate() {

		SchemaValidator schemaValidator = new SchemaValidator(cfg);
		schemaValidator.validate();
	}

	public String[] createUpdateScript(DataSource dataSource, Dialect dialect) throws SQLException {
		DatabaseMetadata meta = new DatabaseMetadata(dataSource.getConnection(), dialect);
		String[] createSQL = cfg.generateSchemaUpdateScript(dialect, meta);
		return createSQL;
	}

	private List<Class> getClasses(String packageName) throws Exception {
		final String classExt = ".class";
		List<String> files = listFilesInPackage(packageName);
		List<Class> classes = new ArrayList<Class>(files.size());

		for (String file : files) {
			if (file.endsWith(classExt)) {
				// removes the .class extension, and replaces '/' with '.'
				String className = (file.substring(0, file.length() - classExt.length())).replace(File.separatorChar,
						'.');
				classes.add(Class.forName(className));
			}
		}

		return classes;
	}

	private List<String> listFilesInPackage(String packageName) throws ClassNotFoundException, IOException {

		log.debug("listFiledInPacakge: starting for package: {}", packageName);

		List<String> classNames = new ArrayList<String>();
		File directory = null;
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}

			String path = packageName.replace('.', '/');

			for (Enumeration<URL> resources = cld.getResources(path); resources.hasMoreElements();) {
				URL resource = resources.nextElement();
				// if (!resource.getFile().toString().toLowerCase().contains("test")) {
				String filePath = resource.getFile();
				filePath = filePath.replace("%20", " "); // In case of directory spacing (marked as %20 in Jenkins
															// workspace)
				directory = new File(filePath);
				break;
				// }

			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(packageName + " (" + directory + ") does not appear to be a valid package");
		}

		if (directory.exists()) { // Deal with file-system case

			log.debug("listFiledInPacakge: directory for path exists: {}", directory.getAbsolutePath());

			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				classNames.add(packageName.replace('.', File.separatorChar) + '.' + files[i]);
			}
		} else { // Deal with case where files are within a JAR

			log.debug("listFiledInPacakge: directory for path doesn't exist: absolute path = {}, path = {}",
					directory.getAbsoluteFile(), directory.getPath());

			final String[] parts = directory.getPath().split(".jar!\\\\");

			log.debug("listFiledInPacakge: after splitting the path {}, got {} parts", directory.getPath(),
					parts.length);

			if (parts.length == 2) {
				String jarFilename = parts[0].substring(6) + ".jar";
				String relativePath = parts[1].replace(File.separatorChar, '/');
				JarFile jarFile = new JarFile(jarFilename);
				final Enumeration entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = (JarEntry) entries.nextElement();
					final String entryName = entry.getName();
					if ((entryName.length() > relativePath.length()) && entryName.startsWith(relativePath)) {
						classNames.add(entryName.replace('/', File.separatorChar));
					}
				}
			} else {
				throw new ClassNotFoundException(packageName + " is not a valid package");
			}
		}

		return classNames;
	}
}
