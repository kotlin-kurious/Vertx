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
    vertx.deployVerticle(ServerVerticle())

}

class ServerVerticle : CoroutineVerticle() {
    override suspend fun start() {
        val router = router()

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8081)
        println("open http://localhost:8081/status")
    }

    private fun router(): Router {

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/status").handler { ctx ->
            val json = json {
                obj(
                    "status" to "OK"
                )
            }

            ctx.response()
                .setStatusCode(200)
                .end(json.toString())
        }


        return router
    }
}