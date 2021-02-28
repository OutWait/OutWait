package edu.kit.outwait.server.core

import java.util.Date

/** Simple logger class that encapsulates logging logic. */
object Logger {

    /**
     * Logs a debug message
     *
     * @param origin source of the log usually an abbreviation of the class name
     * @param message the actual text of the log
     */
    fun debug(origin: String, message: String) {
        println(Date().toString() + " [DEBUG] " + origin + ": " + message)
        // simple implementation which could be extendend in future
    }

    /**
     * Logs an information string
     *
     * @param origin source of the log usually an abbreviation of the class name
     * @param message the actual text of the log
     */
    fun info(origin: String, message: String) {
        println(Date().toString() + " [INFO] " + origin + ": " + message)
        // simple implementation which could be extendend in future
    }

    /**
     * Logs an error message
     *
     * @param origin source of the log usually an abbreviation of the class name
     * @param message the actual text of the log
     */
    fun error(origin: String, message: String) {
        println(Date().toString() + " [ERROR] " + origin + ": " + message)
        // simple implementation which could be extendend in future
    }

    /**
     * Logs an error message of an internal error
     *
     * @param origin source of the log usually an abbreviation of the class name
     * @param message the actual text of the log
     */
    fun internalError(origin: String, message: String) {
        println(Date().toString() + " [INTERNAL ERROR] " + origin + ": " + message)
        // simple implementation which could be extendend in future
    }
}
