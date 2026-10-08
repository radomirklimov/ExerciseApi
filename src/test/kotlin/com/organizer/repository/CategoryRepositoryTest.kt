package com.organizer.repository

import com.organizer.entity.CategoryEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager

@DataJpaTest
class CategoryRepositoryTest(
    @Autowired val categoryRepo: CategoryRepository,
    @Autowired val em: TestEntityManager,
) {
    @Test
    fun `sports have no parent, categories have one`() {
        val gym = em.persist(CategoryEntity(name = "Gym", parent = null))
        em.persist(CategoryEntity(name = "Chest", parent = gym))
        em.flush()
        em.clear()

        val sports = categoryRepo.findAllSports()
        val categories = categoryRepo.findAllCategories()

        assertThat(sports.map { it.name }).containsExactly("Gym")
        assertThat(categories.map { it.name }).containsExactly("Chest")
        assertThat(categories.single().parent?.categoryId).isEqualTo(gym.categoryId)
    }
}
