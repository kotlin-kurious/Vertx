import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

fun main() {
    val vertx = Vertx.vertx()
    vertx.createHttpServer().requestHandler{ ctx ->
        ctx.response().end("OK")
    }.listen(8081)
    println("open http://localhost:8081")
}