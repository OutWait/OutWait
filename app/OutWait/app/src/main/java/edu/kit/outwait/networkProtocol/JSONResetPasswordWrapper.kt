package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "resetPassword@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONResetPasswordWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param username as String, the user account for which the password is to be reset
     */
    constructor(username: String) : this(JSONObject()) {
        jsonObj.put(USERNAME, username)
    }

}
