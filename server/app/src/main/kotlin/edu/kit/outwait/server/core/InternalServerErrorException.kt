package edu.kit.outwait.server.core

/**
 * Exception class for internal errors that should be sent to the user.
 *
 * @param message the message string to send.
 */
class InternalServerErrorException(message: String) : Exception(message)
