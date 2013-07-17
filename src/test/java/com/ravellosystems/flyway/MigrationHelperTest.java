package com.ravellosystems.flyway;

import java.sql.Driver;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A helper test that suggest an sql update script to be applied to a given db in order to make it compatible with the
 * given entities (that are read from the classpath; modify com.ravellosystems.flyway.EntityPackagesScanner.BASE_PACKAGE
 * to set the base package of your entities).
 * 
 * to use it, edit the DB related properties (url, username, password, driver, dialect etc.) to reflect your existing DB
 * instance, and run as a TestNG test.
 * 
 * Note you can get the suggested script for a given dialect by setting it in schemaGenerator.createUpdateScript()
 * invocation (it can be different than the actual DB dialect, e.g. when using H2 compatibility mode).
 * 
 * @author ravello systems
 * 
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { EntityPackagesScanner.class })
public class MigrationHelperTest extends AbstractTestNGSpringContextTests {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private EntityPackagesScanner entityPackageScanner;

	// edit those properties according to your env/needs...
	private final String dbUrl = "jdbc:h2:/tmp/h2db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;TRACE_LEVEL_SYSTEM_OUT=1";
	private final String dbUsername = "username";
	private final String dbPassword = "secret";
	private static final Class<? extends Dialect> dialectClass = H2Dialect.class;
	private static final Class<? extends java.sql.Driver> driverClass = org.h2.Driver.class;

	private DataSource dataSource;
	private SchemaGenerator schemaGenerator;

	@BeforeMethod
	protected void createSchemaGenerator() throws Exception {
		schemaGenerator = new SchemaGenerator(dialectClass, driverClass, dbUrl, dbUsername, dbPassword,
				entityPackageScanner.getEntityPackages());
	}

	@BeforeClass
	protected void setupDatasource() throws Exception {

		Driver driver = driverClass.newInstance();
		log.info("working with db in url {}", dbUrl);
		dataSource = new SimpleDriverDataSource(driver, dbUrl, dbUsername, dbPassword);
	}

	private String formatSqlSuggestionScript(String[] sqls) {
		StringBuilder builder = new StringBuilder("\n\n****update sql suggestion:***\n\n");
		for (String str : sqls) {
			builder.append("\n" + str + ";");
		}
		builder.append("\n\n");
		return builder.toString();
	}

	@Test
	public void suggestSqlUpdateScript() throws SQLException {

		String[] updateCommands = schemaGenerator.createUpdateScript(dataSource, new PostgreSQL82Dialect());
		Assert.assertEquals(updateCommands.length, 0, formatSqlSuggestionScript(updateCommands));
	}

}
