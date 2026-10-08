package com.organizer.repository

import com.organizer.entity.ExerciseEntity
import com.organizer.entity.ExerciseImageEntity
import com.organizer.entity.ExerciseInstructionEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DataJpaTest
class ExerciseRepositoryTest(
    @Autowired val exerciseRepo: ExerciseRepository,
    @Autowired val em: TestEntityManager,
) {
    private fun seed(): ExerciseEntity {
        val exercise = ExerciseEntity(name = "Push Up")
        // saved out of order on purpose: mapping must come back ordered by position
        exercise.instructions.add(ExerciseInstructionEntity(text = "second", position = 2, exercise = exercise))
        exercise.instructions.add(ExerciseInstructionEntity(text = "first", position = 1, exercise = exercise))
        exercise.images.add(ExerciseImageEntity(imageUrl = "b.png", position = 2, exercise = exercise))
        exercise.images.add(ExerciseImageEntity(imageUrl = "a.png", position = 1, exercise = exercise))
        return em.persist(exercise).also { em.flush(); em.clear() }
    }

    @Test
    fun `details query fetches instructions and images ordered by position`() {
        val saved = seed()

        val loaded = exerciseRepo.findAllWithDetails(listOf(exerciseRepo.findById(saved.exerciseId).orElseThrow()))

        assertThat(loaded).hasSize(1)
        assertThat(loaded.single().instructions.map { it.text }).containsExactly("first", "second")
        assertThat(loaded.single().images.map { it.imageUrl }).containsExactly("a.png", "b.png")
    }

    @Test
    fun `paged findAll slices by page`() {
        repeat(5) { em.persist(ExerciseEntity(name = "Ex $it")) }
        em.flush(); em.clear()

        val page0 = exerciseRepo.findAll(PageRequest.of(0, 2, Sort.by("exerciseId")))
        val page2 = exerciseRepo.findAll(PageRequest.of(2, 2, Sort.by("exerciseId")))

        assertThat(page0.content.map { it.name }).containsExactly("Ex 0", "Ex 1")
        assertThat(page2.content.map { it.name }).containsExactly("Ex 4")
        assertThat(page0.totalElements).isEqualTo(5)
    }
}
