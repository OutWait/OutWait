package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * This is the base class for the JSONObjectWrapper
 *
 * @property jsonObj the given JSONObject that is to be wrapped
 */
abstract class JSONObjectWrapper(protected val jsonObj: JSONObject) {

    /**
     * Getter for the wrapped JSONObject
     *
     * @return JSONObject that is being wrapped
     */
    fun getJSONObject(): JSONObject {
        return jsonObj
    }

    /**
     * Getter for the wrapped JSONObject as a JSON String
     *
     * @return JSONObject that is being wrapped, as a JSON String
     */
    fun getJSONString(): String{
        return jsonObj.toString()
    }
}
