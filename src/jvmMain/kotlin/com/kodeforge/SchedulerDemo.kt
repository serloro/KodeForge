package com.kodeforge

import com.kodeforge.data.repository.JvmFileSystemAdapter
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.domain.usecases.PlanningUseCases
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Demostración del Scheduler Secuencial MVP.
 * 
 * Carga data-schema.json, genera el schedule y muestra los resultados.
 */
fun main() = runBlocking {
    println("=".repeat(80))
    println("SCHEDULER SECUENCIAL MVP - DEMOSTRACIÓN")
    println("=".repeat(80))
    println()
    
    // 1. Cargar workspace desde data-schema.json
    val fileSystem = JvmFileSystemAdapter()
    val repository = WorkspaceRepository(fileSystem)
    
    val workspacePath = "specs/data-schema.json"
    
    println("📂 Cargando workspace desde: $workspacePath")
    val workspace = repository.load(workspacePath)
    println("✅ Workspace cargado")
    println()
    
    // 2. Mostrar información del workspace
    println("📊 INFORMACIÓN DEL WORKSPACE:")
    println("-".repeat(80))
    println("Personas: ${workspace.people.size}")
    workspace.people.forEach { person ->
        println("  • ${person.displayName} (${person.role}): ${person.hoursPerDay}h/día - ${if (person.active) "Activo" else "Inactivo"}")
    }
    println()
    
    println("Proyectos: ${workspace.projects.size}")
    workspace.projects.forEach { project ->
        println("  • ${project.name} (${project.status})")
    }
    println()
    
    println("Tareas: ${workspace.tasks.size}")
    workspace.tasks.forEach { task ->
        val assignee = workspace.people.find { it.id == task.assigneeId }
        val pendingHours = task.costHours - task.doneHours
        println("  • [P${task.priority}] ${task.title}")
        println("    Asignada a: ${assignee?.displayName ?: "Sin asignar"}")
        println("    Costo: ${task.costHours}h | Hechas: ${task.doneHours}h | Pendientes: ${pendingHours}h")
        println("    Estado: ${task.status}")
    }
    println()
    
    // 3. Generar schedule
    println("⚙️ GENERANDO SCHEDULE...")
    println("-".repeat(80))
    
    val planningUseCases = PlanningUseCases()
    val startDate = LocalDate(2026, 2, 17) // Lunes 17 de febrero
    
    println("Fecha de inicio: $startDate (Lunes)")
    println("Días laborables: Lun-Vie")
    println()
    
    val result = planningUseCases.generateSchedule(workspace, startDate)
    
    if (result.isFailure) {
        println("❌ Error generando schedule: ${result.exceptionOrNull()?.message}")
        return@runBlocking
    }
    
    val updatedWorkspace = result.getOrThrow()
    val planning = updatedWorkspace.planning
    
    println("✅ Schedule generado exitosamente")
    println("   Generado en: ${planning.generatedAt}")
    println("   Estrategia: ${planning.strategy.type}")
    println("   Bloques generados: ${planning.scheduleBlocks.size}")
    println()
    
    // 4. Mostrar schedule por persona
    println("📅 SCHEDULE POR PERSONA:")
    println("=".repeat(80))
    
    workspace.people.filter { it.active }.forEach { person ->
        val blocks = planningUseCases.getScheduleForPerson(updatedWorkspace, person.id)
        
        if (blocks.isEmpty()) {
            println("👤 ${person.displayName} (${person.hoursPerDay}h/día)")
            println("   Sin tareas asignadas")
            println()
            return@forEach
        }
        
        println("👤 ${person.displayName} (${person.hoursPerDay}h/día)")
        println("-".repeat(80))
        
        val groupedByDate = blocks.groupBy { it.date }
        
        groupedByDate.forEach { (date, dateBlocks) ->
            val totalHours = dateBlocks.sumOf { it.hoursPlanned }
            println("   📆 $date (${totalHours}h)")
            
            dateBlocks.forEach { block ->
                val task = workspace.tasks.find { it.id == block.taskId }
                println("      • ${task?.title ?: "Unknown"} - ${block.hoursPlanned}h")
            }
        }
        
        val endDate = planningUseCases.getEstimatedEndDate(updatedWorkspace, person.id)
        println("   🏁 Fecha estimada de finalización: $endDate")
        println()
    }
    
    // 5. Mostrar schedule por fecha
    println("📅 SCHEDULE POR FECHA:")
    println("=".repeat(80))
    
    val allDates = planning.scheduleBlocks.map { it.date }.distinct().sorted()
    
    allDates.take(5).forEach { date ->
        val blocks = planningUseCases.getScheduleForDate(updatedWorkspace, date)
        val totalHours = blocks.sumOf { it.hoursPlanned }
        
        println("📆 $date (${blocks.size} bloques, ${totalHours}h total)")
        println("-".repeat(80))
        
        blocks.forEach { block ->
            val person = workspace.people.find { it.id == block.personId }
            val task = workspace.tasks.find { it.id == block.taskId }
            println("   • ${person?.displayName ?: "Unknown"}: ${task?.title ?: "Unknown"} - ${block.hoursPlanned}h")
        }
        println()
    }
    
    if (allDates.size > 5) {
        println("   ... y ${allDates.size - 5} fechas más")
        println()
    }
    
    // 6. Guardar workspace actualizado (opcional)
    println("💾 GUARDANDO WORKSPACE ACTUALIZADO...")
    println("-".repeat(80))
    
    val outputPath = "workspace-with-schedule.json"
    repository.save(outputPath, updatedWorkspace)
    
    println("✅ Workspace guardado en: $outputPath")
    println()
    
    // 7. Resumen final
    println("📊 RESUMEN FINAL:")
    println("=".repeat(80))
    println("✅ Tareas scheduladas: ${workspace.tasks.count { it.assigneeId != null && it.status != "completed" }}")
    println("✅ Bloques generados: ${planning.scheduleBlocks.size}")
    println("✅ Personas con schedule: ${planning.scheduleBlocks.map { it.personId }.distinct().size}")
    println("✅ Días planificados: ${allDates.size}")
    println()
    
    println("=".repeat(80))
    println("DEMOSTRACIÓN COMPLETADA")
    println("=".repeat(80))
}

