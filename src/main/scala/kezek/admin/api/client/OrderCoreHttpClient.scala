package kezek.admin.api.client

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, Uri}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class OrderCoreHttpClient(implicit val actorSystem: ActorSystem[_],
                          implicit val executionContext: ExecutionContext) extends HttpClient {


  private val config: Config = ConfigFactory.load();
  private val url = config.getString("endpoints.order-core.url")
  private val log: Logger = LoggerFactory.getLogger(getClass.getSimpleName)

  def findAllOrders(params: Map[String, String]): Future[Json] = {
    log.debug("findAllOrders() was called {params: {}}", params)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/orders").withQuery(Query(params)),
      )
    )
  }

  def getOrderById(id: String): Future[Json] = {
    log.debug("getOrderById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/orders/$id"),
      )
    )
  }

  def createOrder(body: Json): Future[Json] = {
    log.debug("createOrder() was called {body: {}}", body.noSpaces)

    sendRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"$url/orders"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateOrder(id: String, body: Json): Future[Json] = {
    log.debug("updateOrder() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/orders/$id"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateOrderStatus(id: String, body: Json): Future[Json] = {
    log.debug("updateOrderStatus() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/orders/$id/status"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def deleteOrder(id: String): Future[Json] = {
    log.debug("deleteOrder() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/orders/$id"),
      )
    )
  }
}
