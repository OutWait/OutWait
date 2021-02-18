package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.Queue
import org.json.JSONObject

/**
 * NOTE: This class does not implement a queue-getter, because the server will not receive a queue
 * (as it is the single source of truth) and the server does not implement the "gravity-queue"
 * algorithm to parse a json into a valid queue.
 */
class JSONQueueWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setQueue(queue: Queue) {
        queue.storeToJSON(obj)
    }
}
