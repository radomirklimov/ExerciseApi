-- V5: store the exercise type on the exercise entity itself.

ALTER TABLE exercise
    ADD COLUMN IF NOT EXISTS exercise_type VARCHAR(255);

UPDATE exercise
SET exercise_type = CASE
    WHEN lower(name) LIKE '%plank%' OR lower(name) LIKE '%hold%' OR
         lower(name) LIKE '%wall sit%' OR lower(name) LIKE '%dead hang%' OR
         lower(name) LIKE '%hollow%' OR lower(name) LIKE '%l-sit%' OR
         lower(name) LIKE '%seconds%' OR lower(name) LIKE '%minute%' THEN 'DURATION'

    WHEN lower(name) LIKE '%push-up%' OR lower(name) LIKE '%push up%' OR
         lower(name) LIKE '%pull-up%' OR lower(name) LIKE '%pull up%' OR
         lower(name) LIKE '%squat%' OR lower(name) LIKE '%lunge%' OR
         lower(name) LIKE '%crunch%' OR lower(name) LIKE '%sit-up%' OR
         lower(name) LIKE '%sit up%' OR lower(name) LIKE '%burpee%' OR
         lower(name) LIKE '%jump%' OR lower(name) LIKE '%dip%' THEN 'REPS'

    ELSE 'REPS_WEIGHT'
END
WHERE exercise_type IS NULL;

ALTER TABLE exercise
    ALTER COLUMN exercise_type SET NOT NULL;
