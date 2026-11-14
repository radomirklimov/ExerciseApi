package com.organizer.controller

import com.organizer.entity.CategoryEntity
import com.organizer.entity.ExerciseEntity
import com.organizer.entity.ExerciseImageEntity
import com.organizer.entity.ExerciseInstructionEntity
import com.organizer.entity.WorkoutEntity
import com.organizer.entity.WorkoutExerciseEntity
import com.organizer.repository.CategoryRepository
import com.organizer.repository.ExerciseCategoryRepository
import com.organizer.repository.ExerciseRepository
import com.organizer.repository.WorkoutRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class StarterWorkoutControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val categoryRepo: CategoryRepository,
    @Autowired val exerciseRepo: ExerciseRepository,
    @Autowired val exerciseCategoryRepo: ExerciseCategoryRepository,
    @Autowired val workoutRepo: WorkoutRepository,
) {
    @BeforeEach
    fun seed() {
        workoutRepo.deleteAll()
        exerciseCategoryRepo.deleteAll()
        exerciseRepo.deleteAll()
        categoryRepo.deleteAll()

        val pushUp = ExerciseEntity(name = "Push Up")
        pushUp.instructions.add(ExerciseInstructionEntity(text = "Lower your body", position = 1, exercise = pushUp))
        pushUp.images.add(ExerciseImageEntity(imageUrl = "pushup.png", position = 1, exercise = pushUp))
        exerciseRepo.save(pushUp)

        val workout = WorkoutEntity(name = "Upper Body Strength", notes = "", starter = true)
        workout.exercises.add(WorkoutExerciseEntity(workout = workout, exercise = pushUp, orderIndex = 0))
        workoutRepo.save(workout)
    }

    @Test
    fun `starter workouts endpoint returns templates`() {
        mockMvc.get("/api/v1/starter-workouts").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].name") { value("Upper Body Strength") }
            jsonPath("$[0].starter") { value(true) }
            jsonPath("$[0].exercises[0].exerciseId") { exists() }
            jsonPath("$[0].exerciseIds[0]") { exists() }
        }
    }

    @Test
    fun `workouts alias returns same templates`() {
        mockMvc.get("/api/v1/workouts").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
        }
    }

    @Test
    fun `starter workout relationships are persisted`() {
        val workouts = workoutRepo.findAllStarterWithDetails()
        assertEquals(1, workouts.size)
        assertEquals(1, workouts.first().exercises.size)
    }
}