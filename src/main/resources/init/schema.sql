CREATE EXTENSION IF NOT EXISTS vector;
-- why create hstore extension? because pgvector depends on it
-- but we don't use hstore in our table
-- so we can ignore it if we want, are you sure? yes, I'm sure
-- but if you want to use hstore in the future, you can keep it
-- so it's better to keep it

-- what is hstore? hstore is a key-value store for PostgreSQL
-- it allows you to store sets of key/value pairs within a single PostgreSQL value
-- for more information, see https://www.postgresql.org/docs/current/hstore.html
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE IF NOT EXISTS vector_store (
                                            id TEXT PRIMARY KEY, -- id should be TEXT (not UUID type)
                                            content TEXT,
                                            metadata JSONB,
                                            embedding VECTOR(768)
    );
CREATE TABLE IF NOT EXISTS  message (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(255),
    content TEXT,
    time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS  laws (
    id SERIAL PRIMARY KEY,
    content TEXT,
    date_published DATE DEFAULT CURRENT_DATE
);



-- Create HNSW index for fast search
-- why use HNSW? because it's the most efficient algorithm for high-dimensional vector search
-- but  column cannot have more than 2000 dimensions for hnsw index
-- so we need to use another index type for higher dimensions
-- but pgvector only supports hnsw and ivfflat index types
-- so we need to use ivfflat index for higher dimensions
-- but ivfflat index requires more configuration and tuning
-- so for simplicity, we will use hnsw index for now

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING HNSW (embedding vector_cosine_ops);