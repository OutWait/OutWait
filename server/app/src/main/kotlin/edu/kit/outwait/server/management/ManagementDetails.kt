package edu.kit.outwait.server.management

/**
 * Data class that stores general information about an institution.
 *
 * @property name the name of the institution, can be presented to the client.
 * @property email the E-Mail address of the institution.
 * @constructor Creates the read-only object.
 */
data class ManagementDetails(val name: String, val email: String)
