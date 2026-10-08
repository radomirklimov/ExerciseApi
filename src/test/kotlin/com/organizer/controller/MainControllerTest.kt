package com.organizer.controller

import com.organizer.entity.CategoryEntity
import com.organizer.entity.ExerciseCategoryEntity
import com.organizer.entity.ExerciseEntity
import com.organizer.entity.ExerciseImageEntity
import com.organizer.entity.ExerciseInstructionEntity
import com.organizer.repository.CategoryRepository
import com.organizer.repository.ExerciseCategoryRepository
import com.organizer.repository.ExerciseRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val categoryRepo: CategoryRepository,
    @Autowired val exerciseRepo: ExerciseRepository,
    @Autowired val exerciseCategoryRepo: ExerciseCategoryRepository,
) {
    @BeforeEach
    fun seed() {
        exerciseCategoryRepo.deleteAll()
        exerciseRepo.deleteAll()
        categoryRepo.deleteAll()

        val gym = categoryRepo.save(CategoryEntity(name = "Gym", parent = null))
        val chest = categoryRepo.save(CategoryEntity(name = "Chest", parent = gym))
        val pushUp = ExerciseEntity(name = "Push Up")
        pushUp.instructions.add(ExerciseInstructionEntity(text = "Lower your body", position = 1, exercise = pushUp))
        pushUp.images.add(ExerciseImageEntity(imageUrl = "pushup.png", position = 1, exercise = pushUp))
        exerciseRepo.save(pushUp)
        exerciseCategoryRepo.save(ExerciseCategoryEntity(exercise = pushUp, category = chest))
    }

    @Test
    fun `sports endpoint returns top-level categories`() {
        mockMvc.get("/api/v1/sports").andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("Gym") }
            jsonPath("$[0].parentCategoryId") { isEmpty() }
        }
    }

    @Test
    fun `exercises endpoint returns paged array`() {
        mockMvc.get("/api/v1/exercises?page=0&size=20").andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("Push Up") }
            jsonPath("$[0].images[0]") { value("pushup.png") }
            jsonPath("$[0].instructions[0]") { value("Lower your body") }
        }
    }

    @Test
    fun `empty page returns empty array`() {
        mockMvc.get("/api/v1/exercises?page=5&size=20").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `exercise-category endpoint returns links`() {
        mockMvc.get("/api/v1/exercise-category").andExpect {
            status { isOk() }
            jsonPath("$[0].exerciseId") { exists() }
            jsonPath("$[0].categoryId") { exists() }
        }
    }
}
