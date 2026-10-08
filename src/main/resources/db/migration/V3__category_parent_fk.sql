-- V3: real foreign key for the category tree (parent_category_id).
-- Old databases already carry a Hibernate-generated FK under a random name;
-- only add the named constraint when no FK exists on that column yet.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attname = 'parent_category_id'
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'category'::regclass
          AND c.contype = 'f'
    ) THEN
        ALTER TABLE category
            ADD CONSTRAINT fk_category_parent
            FOREIGN KEY (parent_category_id) REFERENCES category (category_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_category_parent_category_id ON category (parent_category_id);
