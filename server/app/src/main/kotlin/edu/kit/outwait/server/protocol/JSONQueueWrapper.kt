package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.management.ManagementId
import edu.kit.outwait.server.management.Queue
import edu.kit.outwait.server.management.QueueId
import org.json.JSONObject

class JSONQueueWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setQueue(queue: Queue) {}
    fun getQueue(): Queue { return Queue(ManagementId(0), QueueId(0), DatabaseWrapper()) }
}
