package com.zendalona.zmantra.view

import com.zendalona.zmantra.domain.repository.UserGuideRepository
import com.zendalona.zmantra.domain.usecase.userguide.GetUserGuideHtmlUseCase
import com.zendalona.zmantra.presentation.features.userguide.UserGuideViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UserGuideViewModelTest {

    private lateinit var viewModel: UserGuideViewModel
    private lateinit var fakeRepository: FakeUserGuideRepository
    private lateinit var useCase: GetUserGuideHtmlUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeUserGuideRepository()
        useCase = GetUserGuideHtmlUseCase(fakeRepository)
        viewModel = UserGuideViewModel(useCase)
    }

    @Test
    fun `load user guide updates uiState with styled HTML`() = runTest {
        fakeRepository.htmlContent = "<p>Hello</p>"

        viewModel.loadUserGuide("en", Pair("#FFFFFF", "#000000"))

        val state = viewModel.uiState.value
        assertNotNull(state.styledHtml)
        assertTrue(state.styledHtml!!.contains("Hello"))
        assertFalse(state.isError)
    }

    @Test
    fun `repository throws error sets error state`() = runTest {
        fakeRepository.shouldThrowError = true

        viewModel.loadUserGuide("en", Pair("#FFFFFF", "#000000"))

        val state = viewModel.uiState.value
        assertTrue(state.isError)
    }

    // Fake repository for testing
    class FakeUserGuideRepository : UserGuideRepository {
        var htmlContent: String = ""
        var shouldThrowError: Boolean = false

        override suspend fun getUserGuideHtml(language: String): String {
            if (shouldThrowError) throw Exception("Error loading HTML")
            return htmlContent
        }
    }
}
