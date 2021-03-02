package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.Queue
import org.json.JSONObject

/**
 * Json wrapper for queues.
 *
 * This class does not implement a queue-getter, because the server will not receive a queue (as it
 * is the single source of truth) and the server does not implement the "gravity-queue" algorithm to
 * parse a json into a valid queue.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONQueueWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the queue
     *
     * @param queue the queue
     */
    fun setQueue(queue: Queue) {
        queue.storeToJSON(obj)
    }
}
