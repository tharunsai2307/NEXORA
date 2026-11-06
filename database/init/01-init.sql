-- Database initialization script for PostgreSQL container startup
-- Executed on fresh container startup to configure database extensions and encoding

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Smart Building database initialized with standard extensions.';
END
$$;
