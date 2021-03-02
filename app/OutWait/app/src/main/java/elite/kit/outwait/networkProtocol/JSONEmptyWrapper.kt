package elite.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for events, where no data is to be transmitted or received (except for the event string),
 * hence an empty JSONObject will be transmitted according to the specified (JSON) protocol
 * //TODO Verweis auf Entwurfsdokument oder Protokoll? Bps. JSON syntax
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will be empty for the respective event)
 */
class JSONEmptyWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, creates the JSONWrapper containing an empty JSONObject, that
     * will be transmitted according to the specified (JSON) protocol
     * //TODO Verweis auf Entwurfsdokument oder Protokoll? Bps. JSON syntax
     */
    constructor() : this(JSONObject())

}
