package io.humla.graphql

import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import io.ktor.serialization.jackson.JacksonWebsocketContentConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import java.time.Duration

fun Application.graphQLModule() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(1)
        contentConverter = JacksonWebsocketContentConverter()
    }
    install(GraphQL) {
        schema {
            packages = listOf("io.humla.graphql")
            queries = listOf(ExampleQuery())
            subscriptions = listOf(ExampleSubscription())
        }
    }
    install(Routing) {
        graphQLPostRoute()
        graphQLSubscriptionsRoute()
    }
}