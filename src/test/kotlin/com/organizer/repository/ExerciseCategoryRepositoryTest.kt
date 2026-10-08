package com.organizer.repository

import com.organizer.entity.CategoryEntity
import com.organizer.entity.ExerciseCategoryEntity
import com.organizer.entity.ExerciseEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager

@DataJpaTest
class ExerciseCategoryRepositoryTest(
    @Autowired val exerciseCategoryRepo: ExerciseCategoryRepository,
    @Autowired val em: TestEntityManager,
) {
    @Test
    fun `links resolve exercise and category without extra queries per row`() {
        val gym = em.persist(CategoryEntity(name = "Gym", parent = null))
        val chest = em.persist(CategoryEntity(name = "Chest", parent = gym))
        val pushUp = em.persist(ExerciseEntity(name = "Push Up"))
        em.persist(ExerciseCategoryEntity(exercise = pushUp, category = chest))
        em.flush()
        em.clear()

        val links = exerciseCategoryRepo.findAllByOrderByExerciseCategoryIdAsc()

        assertThat(links).hasSize(1)
        assertThat(links.single().exercise.name).isEqualTo("Push Up")
        assertThat(links.single().category.name).isEqualTo("Chest")
    }
}
