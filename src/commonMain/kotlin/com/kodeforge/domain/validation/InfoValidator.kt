package com.kodeforge.domain.validation

import com.kodeforge.domain.model.Workspace

/**
 * Validador para páginas Info (WYSIWYG multiidioma).
 */
object InfoValidator {
    
    sealed class ValidationError(val message: String) {
        object SlugEmpty : ValidationError("El slug no puede estar vacío")
        object SlugInvalid : ValidationError("El slug solo puede contener letras minúsculas, números y guiones")
        object SlugDuplicate : ValidationError("Ya existe una página con ese slug en este proyecto")
        object TitleEmpty : ValidationError("El título no puede estar vacío")
        object PageNotFound : ValidationError("Página no encontrada")
        object ProjectNotFound : ValidationError("Proyecto no encontrado")
    }
    
    /**
     * Valida el formato del slug.
     * 
     * Reglas:
     * - No vacío
     * - Solo letras minúsculas, números y guiones
     * - Ejemplos válidos: "intro", "api-reference", "getting-started-2"
     */
    fun validateSlug(slug: String): Result<Unit> {
        if (slug.isBlank()) {
            return Result.failure(Exception(ValidationError.SlugEmpty.message))
        }
        
        val slugRegex = Regex("^[a-z0-9-]+$")
        if (!slugRegex.matches(slug)) {
            return Result.failure(Exception(ValidationError.SlugInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida que el título no esté vacío.
     */
    fun validateTitle(title: String): Result<Unit> {
        if (title.isBlank()) {
            return Result.failure(Exception(ValidationError.TitleEmpty.message))
        }
        return Result.success(Unit)
    }
    
    /**
     * Valida que el slug sea único dentro del proyecto.
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param slug Slug a validar
     * @param excludePageId ID de página a excluir (para updates)
     */
    fun validateSlugUnique(
        workspace: Workspace,
        projectId: String,
        slug: String,
        excludePageId: String? = null
    ): Result<Unit> {
        val project = workspace.projects.find { it.id == projectId }
            ?: return Result.failure(Exception(ValidationError.ProjectNotFound.message))
        
        val pages = project.tools.info?.pages ?: emptyList()
        val duplicate = pages.find { 
            it.slug == slug && it.id != excludePageId 
        }
        
        if (duplicate != null) {
            return Result.failure(Exception(ValidationError.SlugDuplicate.message))
        }
        
        return Result.success(Unit)
    }
}

