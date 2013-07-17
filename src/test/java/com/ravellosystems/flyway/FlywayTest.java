package com.ravellosystems.flyway;

import java.sql.Driver;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.googlecode.flyway.core.Flyway;

/**
 * A unit test that run all supplied migration script (found in /src/main/resources/db/migration) on an empty in-memory
 * H2 database, and run Hibernate's schema validation on the resulted DB.
 * 
 * The base package of your entities should be set in com.ravellosystems.flyway.EntityPackagesScanner.BASE_PACKAGE
 * 
 * @author ravello systems
 * 
 */
@Test
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { EntityPackagesScanner.class, })
public class FlywayTest extends AbstractTestNGSpringContextTests {

	@Inject
	private EntityPackagesScanner entityPackageScanner;

	private static final Class<? extends Dialect> dialectClass = H2Dialect.class;

	public void testHibernateHbm2DdlValidationAfterFullFlywayMigrationOnH2() {
		try {
			Driver driver = new org.h2.Driver();
			String dbUsername = "user";
			String dbPassword = "password";
			String dbUrl = "jdbc:h2:mem:migrationtestdb;DB_CLOSE_DELAY=5;MODE=PostgreSQL;TRACE_LEVEL_SYSTEM_OUT=1;";
			DataSource dataSource = new SimpleDriverDataSource(driver, dbUrl, dbUsername, dbPassword);

			Flyway flyway = new Flyway();
			// flyway.setLocations("database/migration");
			flyway.setDataSource(dataSource);
			flyway.clean();
			flyway.init();

			flyway.setValidateOnMigrate(true);
			flyway.migrate();

			SchemaGenerator schemaGenerator = new SchemaGenerator(dialectClass, driver.getClass(), dbUrl, dbUsername,
					dbPassword, entityPackageScanner.getEntityPackages());
			schemaGenerator.validate();
		} catch (Exception e) {
			Assert.fail(
					"Hibernate hbm2ddl validation failed after running Flyway migrations; If you edited entities, make sure to supply a Flyway migration script: "
							+ e.getMessage(), e);
		}
	}
}
