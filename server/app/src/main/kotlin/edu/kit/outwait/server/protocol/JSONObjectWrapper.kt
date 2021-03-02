package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Base class for json wrappers.
 *
 * Add data of a message is packed in a json, that can be (un)wrapped by the dedicated json wrapper.
 * The type of json wrapper is defined in the Event enumeration.
 *
 * @ property obj the internal json object, that is used by all subclasses.
 *
 * @constructor creates a new json wrapper from a json object (this may also be empty).
 */
abstract class JSONObjectWrapper(protected val obj: JSONObject) {
    /**
     * Convenience method to get the json string from this wrapper.
     *
     * @return the json string with the data of this object.
     */
    fun getJSONString(): String = obj.toString()
}
