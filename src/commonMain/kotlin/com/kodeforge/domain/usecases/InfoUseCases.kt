package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.InfoPage
import com.kodeforge.domain.model.InfoPageTranslation
import com.kodeforge.domain.model.InfoTool
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.validation.InfoValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para gestión de páginas Info (WYSIWYG multiidioma).
 */
class InfoUseCases {
    
    /**
     * Crea una nueva página Info.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param slug Slug de la página (URL amigable)
     * @param titleEs Título en español
     * @param titleEn Título en inglés
     * @param htmlEs Contenido HTML en español (opcional)
     * @param htmlEn Contenido HTML en inglés (opcional)
     * @return Workspace actualizado
     */
    fun createPage(
        workspace: Workspace,
        projectId: String,
        slug: String,
        titleEs: String,
        titleEn: String,
        htmlEs: String = "",
        htmlEn: String = ""
    ): Result<Workspace> {
        try {
            // Validar slug
            val slugValidation = InfoValidator.validateSlug(slug)
            if (slugValidation.isFailure) {
                return Result.failure(slugValidation.exceptionOrNull()!!)
            }
            
            // Validar unicidad de slug
            val uniqueValidation = InfoValidator.validateSlugUnique(workspace, projectId, slug)
            if (uniqueValidation.isFailure) {
                return Result.failure(uniqueValidation.exceptionOrNull()!!)
            }
            
            // Validar títulos
            val titleEsValidation = InfoValidator.validateTitle(titleEs)
            if (titleEsValidation.isFailure) {
                return Result.failure(titleEsValidation.exceptionOrNull()!!)
            }
            
            val titleEnValidation = InfoValidator.validateTitle(titleEn)
            if (titleEnValidation.isFailure) {
                return Result.failure(titleEnValidation.exceptionOrNull()!!)
            }
            
            // Buscar proyecto
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentInfo = project.tools.info ?: InfoTool(enabled = true, pages = emptyList())
            val currentPages = currentInfo.pages
            
            // Calcular order (último + 1)
            val maxOrder = currentPages.maxOfOrNull { it.order } ?: 0
            val newOrder = maxOrder + 1
            
            // Crear página
            val timestamp = generateTimestamp()
            val newPage = InfoPage(
                id = generateId("info"),
                slug = slug,
                title = mapOf(
                    "es" to titleEs,
                    "en" to titleEn
                ),
                order = newOrder,
                translations = mapOf(
                    "es" to InfoPageTranslation(
                        html = htmlEs,
                        updatedAt = timestamp
                    ),
                    "en" to InfoPageTranslation(
                        html = htmlEn,
                        updatedAt = timestamp
                    )
                )
            )
            
            // Actualizar proyecto
            val updatedInfo = currentInfo.copy(
                pages = currentPages + newPage
            )
            
            val updatedTools = project.tools.copy(info = updatedInfo)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = timestamp
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Actualiza una página existente.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param pageId ID de la página
     * @param slug Nuevo slug (opcional)
     * @param titleEs Nuevo título en español (opcional)
     * @param titleEn Nuevo título en inglés (opcional)
     * @param htmlEs Nuevo contenido HTML en español (opcional)
     * @param htmlEn Nuevo contenido HTML en inglés (opcional)
     * @return Workspace actualizado
     */
    fun updatePage(
        workspace: Workspace,
        projectId: String,
        pageId: String,
        slug: String? = null,
        titleEs: String? = null,
        titleEn: String? = null,
        htmlEs: String? = null,
        htmlEn: String? = null
    ): Result<Workspace> {
        try {
            // Buscar proyecto
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentInfo = project.tools.info
                ?: return Result.failure(Exception("Info tool no habilitado"))
            
            val currentPages = currentInfo.pages
            val pageIndex = currentPages.indexOfFirst { it.id == pageId }
            if (pageIndex == -1) {
                return Result.failure(Exception(InfoValidator.ValidationError.PageNotFound.message))
            }
            
            val existingPage = currentPages[pageIndex]
            
            // Validar slug si se proporciona
            val finalSlug = slug ?: existingPage.slug
            if (slug != null) {
                val slugValidation = InfoValidator.validateSlug(slug)
                if (slugValidation.isFailure) {
                    return Result.failure(slugValidation.exceptionOrNull()!!)
                }
                
                val uniqueValidation = InfoValidator.validateSlugUnique(
                    workspace, projectId, slug, excludePageId = pageId
                )
                if (uniqueValidation.isFailure) {
                    return Result.failure(uniqueValidation.exceptionOrNull()!!)
                }
            }
            
            // Validar títulos si se proporcionan
            val finalTitleEs = titleEs ?: existingPage.title["es"] ?: ""
            val finalTitleEn = titleEn ?: existingPage.title["en"] ?: ""
            
            if (titleEs != null) {
                val validation = InfoValidator.validateTitle(titleEs)
                if (validation.isFailure) {
                    return Result.failure(validation.exceptionOrNull()!!)
                }
            }
            
            if (titleEn != null) {
                val validation = InfoValidator.validateTitle(titleEn)
                if (validation.isFailure) {
                    return Result.failure(validation.exceptionOrNull()!!)
                }
            }
            
            // Actualizar traducciones
            val timestamp = generateTimestamp()
            val existingTranslations = existingPage.translations
            
            val updatedTranslationEs = if (htmlEs != null || titleEs != null) {
                InfoPageTranslation(
                    html = htmlEs ?: existingTranslations["es"]?.html ?: "",
                    updatedAt = timestamp
                )
            } else {
                existingTranslations["es"] ?: InfoPageTranslation("", timestamp)
            }
            
            val updatedTranslationEn = if (htmlEn != null || titleEn != null) {
                InfoPageTranslation(
                    html = htmlEn ?: existingTranslations["en"]?.html ?: "",
                    updatedAt = timestamp
                )
            } else {
                existingTranslations["en"] ?: InfoPageTranslation("", timestamp)
            }
            
            // Crear página actualizada
            val updatedPage = existingPage.copy(
                slug = finalSlug,
                title = mapOf(
                    "es" to finalTitleEs,
                    "en" to finalTitleEn
                ),
                translations = mapOf(
                    "es" to updatedTranslationEs,
                    "en" to updatedTranslationEn
                )
            )
            
            // Actualizar proyecto
            val updatedPages = currentPages.toMutableList()
            updatedPages[pageIndex] = updatedPage
            
            val updatedInfo = currentInfo.copy(pages = updatedPages)
            val updatedTools = project.tools.copy(info = updatedInfo)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = timestamp
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Elimina una página.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param pageId ID de la página
     * @return Workspace actualizado
     */
    fun deletePage(
        workspace: Workspace,
        projectId: String,
        pageId: String
    ): Result<Workspace> {
        try {
            // Buscar proyecto
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentInfo = project.tools.info
                ?: return Result.failure(Exception("Info tool no habilitado"))
            
            val currentPages = currentInfo.pages
            val pageExists = currentPages.any { it.id == pageId }
            if (!pageExists) {
                return Result.failure(Exception(InfoValidator.ValidationError.PageNotFound.message))
            }
            
            // Eliminar página
            val updatedPages = currentPages.filter { it.id != pageId }
            
            // Actualizar proyecto
            val timestamp = generateTimestamp()
            val updatedInfo = currentInfo.copy(pages = updatedPages)
            val updatedTools = project.tools.copy(info = updatedInfo)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = timestamp
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Reordena páginas.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param pageIds Lista de IDs de páginas en el nuevo orden
     * @return Workspace actualizado
     */
    fun reorderPages(
        workspace: Workspace,
        projectId: String,
        pageIds: List<String>
    ): Result<Workspace> {
        try {
            // Buscar proyecto
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentInfo = project.tools.info
                ?: return Result.failure(Exception("Info tool no habilitado"))
            
            val currentPages = currentInfo.pages
            
            // Verificar que todos los IDs existen
            val existingIds = currentPages.map { it.id }.toSet()
            val requestedIds = pageIds.toSet()
            
            if (requestedIds != existingIds) {
                return Result.failure(Exception("Los IDs de páginas no coinciden"))
            }
            
            // Reordenar páginas
            val reorderedPages = pageIds.mapIndexed { index, pageId ->
                val page = currentPages.find { it.id == pageId }!!
                page.copy(order = index + 1)
            }
            
            // Actualizar proyecto
            val timestamp = generateTimestamp()
            val updatedInfo = currentInfo.copy(pages = reorderedPages)
            val updatedTools = project.tools.copy(info = updatedInfo)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = timestamp
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Obtiene una página por slug.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param slug Slug de la página
     * @return Página encontrada o null
     */
    fun getPageBySlug(
        workspace: Workspace,
        projectId: String,
        slug: String
    ): InfoPage? {
        val project = workspace.projects.find { it.id == projectId } ?: return null
        val pages = project.tools.info?.pages ?: return null
        return pages.find { it.slug == slug }
    }
    
    /**
     * Obtiene todas las páginas ordenadas.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @return Lista de páginas ordenadas
     */
    fun getPages(
        workspace: Workspace,
        projectId: String
    ): List<InfoPage> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        val pages = project.tools.info?.pages ?: return emptyList()
        return pages.sortedBy { it.order }
    }
    
    /**
     * Genera un ID único.
     */
    private fun generateId(prefix: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        return "${prefix}_${timestamp}_${random}"
    }
    
    /**
     * Genera un timestamp ISO 8601.
     */
    private fun generateTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        return "${localDateTime.date}T${localDateTime.time}Z"
    }
}

