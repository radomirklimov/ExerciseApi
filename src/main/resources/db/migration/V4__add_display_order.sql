-- Explicit presentation order. Preserve the existing ID-based order as the initial order.
ALTER TABLE category
    ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;

WITH ordered_categories AS (
    SELECT category_id,
           ROW_NUMBER() OVER (
               PARTITION BY parent_category_id
               ORDER BY category_id
           ) - 1 AS position
    FROM category
)
UPDATE category c
SET sort_order = ordered_categories.position
FROM ordered_categories
WHERE c.category_id = ordered_categories.category_id;

ALTER TABLE exercise_category
    ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;

WITH ordered_exercises AS (
    SELECT exercise_category_id,
           ROW_NUMBER() OVER (
               PARTITION BY category_id
               ORDER BY exercise_category_id
           ) - 1 AS position
    FROM exercise_category
)
UPDATE exercise_category ec
SET sort_order = ordered_exercises.position
FROM ordered_exercises
WHERE ec.exercise_category_id = ordered_exercises.exercise_category_id;
