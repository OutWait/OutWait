package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Json wrapper that contains no data.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONEmptyWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())
}
