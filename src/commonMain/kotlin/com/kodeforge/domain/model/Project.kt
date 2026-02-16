package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String = "active", // active, paused, completed, cancelled
    val members: List<String> = emptyList(), // IDs de personas
    val createdAt: String,
    val updatedAt: String,
    val tools: ProjectTools = ProjectTools()
)

@Serializable
data class ProjectTools(
    val smtpFake: SmtpFakeTool? = null,
    val restSoap: RestSoapTool? = null,
    val sftp: SftpTool? = null,
    val dbTools: DbTool? = null,
    val taskManager: TaskManagerTool? = null,
    val info: InfoTool? = null
)

// ===== SMTP Fake Tool =====
@Serializable
data class SmtpFakeTool(
    val enabled: Boolean = false,
    val listenHost: String = "127.0.0.1",
    val listenPort: Int = 2525,
    val allowedRecipients: List<String> = emptyList(),
    val storedInbox: List<EmailMessage> = emptyList()
)

@Serializable
data class EmailMessage(
    val id: String,
    val receivedAt: String,
    val from: String,
    val to: List<String>,
    val subject: String,
    val bodyText: String,
    val headers: Map<String, String> = emptyMap()
)

// ===== REST/SOAP Tool =====
@Serializable
data class RestSoapTool(
    val enabled: Boolean = false,
    val clientHistory: List<HttpRequest> = emptyList(),
    val mockServer: MockServer? = null
)

@Serializable
data class HttpRequest(
    val id: String,
    val at: String,
    val type: String, // REST, SOAP
    val method: String, // GET, POST, etc.
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val response: HttpResponse? = null
)

@Serializable
data class HttpResponse(
    val status: Int,
    val body: String? = null,
    val headers: Map<String, String> = emptyMap()
)

@Serializable
data class MockServer(
    val enabled: Boolean = false,
    val listenHost: String = "127.0.0.1",
    val listenPort: Int = 8089,
    val mode: String = "catchAll", // catchAll, routesOnly
    val routes: List<MockRoute> = emptyList(),
    val capturedRequests: List<CapturedRequest> = emptyList()
)

@Serializable
data class MockRoute(
    val id: String,
    val method: String,
    val path: String,
    val response: HttpResponse
)

@Serializable
data class CapturedRequest(
    val id: String,
    val at: String,
    val method: String,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

// ===== SFTP Tool =====
@Serializable
data class SftpTool(
    val enabled: Boolean = false,
    val connections: List<SftpConnection> = emptyList()
)

@Serializable
data class SftpConnection(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val auth: AuthConfig
)

@Serializable
data class AuthConfig(
    val type: String, // password, key
    val valueRef: String // Referencia a secret
)

// ===== DB Tool =====
@Serializable
data class DbTool(
    val enabled: Boolean = false,
    val connections: List<DbConnection> = emptyList(),
    val savedQueries: List<SavedQuery> = emptyList()
)

@Serializable
data class DbConnection(
    val id: String,
    val name: String,
    val type: String, // postgres, mysql, sqlite, etc.
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val auth: AuthConfig
)

@Serializable
data class SavedQuery(
    val id: String,
    val name: String,
    val connectionId: String,
    val sql: String
)

// ===== Task Manager Tool =====
@Serializable
data class TaskManagerTool(
    val enabled: Boolean = false,
    val integrations: TaskIntegrations = TaskIntegrations()
)

@Serializable
data class TaskIntegrations(
    val github: GithubIntegration? = null
)

@Serializable
data class GithubIntegration(
    val enabled: Boolean = false,
    val repo: String? = null,
    val tokenRef: String? = null
)

// ===== Info Tool (WYSIWYG HTML multiidioma) =====
@Serializable
data class InfoTool(
    val enabled: Boolean = false,
    val pages: List<InfoPage> = emptyList()
)

@Serializable
data class InfoPage(
    val id: String,
    val slug: String,
    val title: Map<String, String>, // { "es": "Título", "en": "Title" }
    val order: Int = 0,
    val translations: Map<String, InfoPageTranslation> // { "es": {...}, "en": {...} }
)

@Serializable
data class InfoPageTranslation(
    val html: String,
    val updatedAt: String
)

