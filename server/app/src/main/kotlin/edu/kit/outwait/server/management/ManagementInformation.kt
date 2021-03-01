package edu.kit.outwait.server.management

/**
 * Data class that combines runtime-relevant information of a institution.
 *
 * @property details general information about an institution.
 * @property settings the settings of the institution.
 * @constructor Creates the read-only object.
 */
data class ManagementInformation(val details: ManagementDetails, val settings: ManagementSettings)
