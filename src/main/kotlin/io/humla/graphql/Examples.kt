package io.humla.graphql

import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import kotlin.random.Random

private const val DELAY_TIME: Long = 1000

// At least one query required
class ExampleQuery : Query {
    fun ping() = "pong"
}

class ExampleSubscription : Subscription {
    suspend fun random(): Flow<Int> {
        slowMethod()
        return flow {
            var count = 0
            while (true) {
                count++
                emit(Random.nextInt())
                delay(DELAY_TIME)
            }
        }
    }

    private suspend fun slowMethod() {
        // Do something that takes some time and could cause the execution to be suspended
        println("Entered slowMethod")
        yield()
        println("Exiting slowMethod") // Will never get here
    }
}
