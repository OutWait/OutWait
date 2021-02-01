package edu.kit.outwait.server.core

class Server {
    val greeting: String
        get() {
            return "Hello World from server!"
        }
}

fun main() {
    println(Server().greeting)
}
