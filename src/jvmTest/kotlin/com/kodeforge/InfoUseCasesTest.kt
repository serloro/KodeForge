package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.InfoUseCases
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para InfoUseCases - Gestión de páginas Info.
 */
class InfoUseCasesTest {
    
    private lateinit var infoUseCases: InfoUseCases
    private lateinit var initialWorkspace: Workspace
    private lateinit var project: Project
    
    @BeforeEach
    fun setup() {
        infoUseCases = InfoUseCases()
        
        project = Project(
            id = "proj1",
            name = "Test Project",
            description = "Test project for Info pages",
            status = "active",
            members = emptyList(),
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z",
            tools = ProjectTools(
                info = InfoTool(enabled = true, pages = emptyList())
            )
        )
        
        initialWorkspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(project),
            tasks = emptyList()
        )
    }
    
    @Test
    fun `createPage - creates page with default content`() {
        val result = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction",
            htmlEs = "<h1>Introducción</h1>",
            htmlEn = "<h1>Introduction</h1>"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrNull()!!
        
        val updatedProject = updatedWorkspace.projects.find { it.id == project.id }!!
        val pages = updatedProject.tools.info?.pages!!
        
        assertEquals(1, pages.size)
        
        val page = pages[0]
        assertEquals("intro", page.slug)
        assertEquals("Introducción", page.title["es"])
        assertEquals("Introduction", page.title["en"])
        assertEquals("<h1>Introducción</h1>", page.translations["es"]?.html)
        assertEquals("<h1>Introduction</h1>", page.translations["en"]?.html)
        assertEquals(1, page.order)
    }
    
    @Test
    fun `createPage - validates slug format`() {
        // Slug con mayúsculas (inválido)
        val result1 = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "Intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        assertTrue(result1.isFailure)
        assertTrue(result1.exceptionOrNull()?.message?.contains("solo puede contener") == true)
        
        // Slug con espacios (inválido)
        val result2 = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro page",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        assertTrue(result2.isFailure)
        
        // Slug válido
        val result3 = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro-page-2",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        assertTrue(result3.isSuccess)
    }
    
    @Test
    fun `createPage - validates slug uniqueness`() {
        // Crear primera página
        val result1 = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        assertTrue(result1.isSuccess)
        
        // Intentar crear segunda página con mismo slug
        val result2 = infoUseCases.createPage(
            workspace = result1.getOrNull()!!,
            projectId = project.id,
            slug = "intro",
            titleEs = "Otra Introducción",
            titleEn = "Another Introduction"
        )
        assertTrue(result2.isFailure)
        assertTrue(result2.exceptionOrNull()?.message?.contains("Ya existe una página") == true)
    }
    
    @Test
    fun `updatePage - updates title and html`() {
        // Crear página
        val createResult = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction",
            htmlEs = "<h1>Viejo</h1>",
            htmlEn = "<h1>Old</h1>"
        )
        assertTrue(createResult.isSuccess)
        
        val workspace1 = createResult.getOrNull()!!
        val pageId = workspace1.projects[0].tools.info?.pages?.get(0)?.id!!
        
        // Actualizar página
        val updateResult = infoUseCases.updatePage(
            workspace = workspace1,
            projectId = project.id,
            pageId = pageId,
            titleEs = "Nueva Introducción",
            htmlEs = "<h1>Nuevo</h1>"
        )
        assertTrue(updateResult.isSuccess)
        
        val workspace2 = updateResult.getOrNull()!!
        val updatedPage = workspace2.projects[0].tools.info?.pages?.get(0)!!
        
        assertEquals("Nueva Introducción", updatedPage.title["es"])
        assertEquals("Introduction", updatedPage.title["en"]) // No cambió
        assertEquals("<h1>Nuevo</h1>", updatedPage.translations["es"]?.html)
        assertEquals("<h1>Old</h1>", updatedPage.translations["en"]?.html) // No cambió
    }
    
    @Test
    fun `deletePage - removes page`() {
        // Crear dos páginas
        val result1 = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        assertTrue(result1.isSuccess)
        
        val result2 = infoUseCases.createPage(
            workspace = result1.getOrNull()!!,
            projectId = project.id,
            slug = "api",
            titleEs = "API",
            titleEn = "API"
        )
        assertTrue(result2.isSuccess)
        
        val workspace1 = result2.getOrNull()!!
        assertEquals(2, workspace1.projects[0].tools.info?.pages?.size)
        
        // Eliminar primera página
        val pageId = workspace1.projects[0].tools.info?.pages?.get(0)?.id!!
        val deleteResult = infoUseCases.deletePage(
            workspace = workspace1,
            projectId = project.id,
            pageId = pageId
        )
        assertTrue(deleteResult.isSuccess)
        
        val workspace2 = deleteResult.getOrNull()!!
        val pages = workspace2.projects[0].tools.info?.pages!!
        assertEquals(1, pages.size)
        assertEquals("api", pages[0].slug)
    }
    
    @Test
    fun `reorderPages - changes order`() {
        // Crear tres páginas
        var workspace = initialWorkspace
        
        val result1 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        workspace = result1.getOrNull()!!
        
        val result2 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "api",
            titleEs = "API",
            titleEn = "API"
        )
        workspace = result2.getOrNull()!!
        
        val result3 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "faq",
            titleEs = "FAQ",
            titleEn = "FAQ"
        )
        workspace = result3.getOrNull()!!
        
        val pages = workspace.projects[0].tools.info?.pages!!
        assertEquals(3, pages.size)
        
        // Orden original: intro (1), api (2), faq (3)
        val introId = pages.find { it.slug == "intro" }?.id!!
        val apiId = pages.find { it.slug == "api" }?.id!!
        val faqId = pages.find { it.slug == "faq" }?.id!!
        
        // Reordenar: faq, intro, api
        val reorderResult = infoUseCases.reorderPages(
            workspace = workspace,
            projectId = project.id,
            pageIds = listOf(faqId, introId, apiId)
        )
        assertTrue(reorderResult.isSuccess)
        
        val reorderedWorkspace = reorderResult.getOrNull()!!
        val reorderedPages = infoUseCases.getPages(reorderedWorkspace, project.id)
        
        assertEquals(3, reorderedPages.size)
        assertEquals("faq", reorderedPages[0].slug)
        assertEquals(1, reorderedPages[0].order)
        assertEquals("intro", reorderedPages[1].slug)
        assertEquals(2, reorderedPages[1].order)
        assertEquals("api", reorderedPages[2].slug)
        assertEquals(3, reorderedPages[2].order)
    }
    
    @Test
    fun `getPageBySlug - finds page`() {
        // Crear página
        val createResult = infoUseCases.createPage(
            workspace = initialWorkspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        val workspace = createResult.getOrNull()!!
        
        // Buscar por slug
        val page = infoUseCases.getPageBySlug(workspace, project.id, "intro")
        assertNotNull(page)
        assertEquals("intro", page.slug)
        assertEquals("Introducción", page.title["es"])
        
        // Buscar slug inexistente
        val notFound = infoUseCases.getPageBySlug(workspace, project.id, "nonexistent")
        assertEquals(null, notFound)
    }
    
    @Test
    fun `getPages - returns pages sorted by order`() {
        // Crear tres páginas
        var workspace = initialWorkspace
        
        val result1 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "intro",
            titleEs = "Introducción",
            titleEn = "Introduction"
        )
        workspace = result1.getOrNull()!!
        
        val result2 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "api",
            titleEs = "API",
            titleEn = "API"
        )
        workspace = result2.getOrNull()!!
        
        val result3 = infoUseCases.createPage(
            workspace = workspace,
            projectId = project.id,
            slug = "faq",
            titleEs = "FAQ",
            titleEn = "FAQ"
        )
        workspace = result3.getOrNull()!!
        
        // Obtener páginas ordenadas
        val pages = infoUseCases.getPages(workspace, project.id)
        
        assertEquals(3, pages.size)
        assertEquals("intro", pages[0].slug)
        assertEquals(1, pages[0].order)
        assertEquals("api", pages[1].slug)
        assertEquals(2, pages[1].order)
        assertEquals("faq", pages[2].slug)
        assertEquals(3, pages[2].order)
    }
}

