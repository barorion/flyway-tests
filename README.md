flyway-tests
============
Includes two interesting tests:

- **FlywayTest** - runs all the migration scripts that are found in the classpath on an empty, in-memory H2 database instance, and runs Hibernate validation (against the JPA entities that are found on the classpath) against this DB.
Useful to catch migration scripts erros as early as possible, or to catch cases where entites where modified without a migration script.

- **MigrationHelperTest** - dry-run Hibernate's hbm2ddl 'update' against a given (existing) DB, using the entities found in the classpath. Prints out the resulted SQL statments, that can be used as a base for the need migration script.

See javadoc for more details.

Note you'll probably need to change some parameters in the code to reflect your DB, and you JPA entities package.
