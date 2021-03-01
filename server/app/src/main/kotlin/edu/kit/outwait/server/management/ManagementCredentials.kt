package edu.kit.outwait.server.management

/**
 * Data class that stores login information of an institution and its corresponding id.
 *
 * @property id the id of the management.
 * @property username the login-name of the management (must not be the actual name of the
 *     institution).
 * @property password the secret which is exchanged with the server in order to ensure authenticity.
 * @constructor Creates the read-only object.
 */
data class ManagementCredentials(val id: ManagementId, val username: String, val password: String)
