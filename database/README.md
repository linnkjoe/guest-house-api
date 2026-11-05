# Database

The application uses PostgreSQL. On a new database, Hibernate creates and updates
the schema from the JPA entities (`DDL_AUTO=update`). Use a persistent Render
PostgreSQL database; do not use an ephemeral database for production data.

The SQL scripts previously in this directory targeted Oracle and have been
removed. This configuration does not migrate data from an existing Oracle
database. Export/import and validate existing data separately before switching
production traffic.

For production systems with ongoing schema changes, replace Hibernate's `update`
mode with versioned PostgreSQL migrations before setting `DDL_AUTO=validate`.
