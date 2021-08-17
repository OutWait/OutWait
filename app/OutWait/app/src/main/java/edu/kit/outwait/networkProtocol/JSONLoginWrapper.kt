package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "managementLogin@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONLoginWrapper(jsonObj: JSONObject): JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param username as String, specifies the user account for the requested login
     * @param password as String, specifies the password for the requested login
     */
    constructor(username: String, password: String) : this(JSONObject()) {
        jsonObj.put(USERNAME, username)
        jsonObj.put(PASSWORD, password)
    }

}
