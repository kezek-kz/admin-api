package kezek.admin.api.client

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, Multipart, Uri}
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

class ReservationCoreHttpClient(implicit val actorSystem: ActorSystem[_],
                                implicit val executionContext: ExecutionContext) extends HttpClient {


  private val config: Config = ConfigFactory.load();
  private val url = config.getString("endpoints.reservation-core.url")
  private val log: Logger = LoggerFactory.getLogger(getClass.getSimpleName)

  def findAllReservations(params: Map[String, String]): Future[Json] = {
    log.debug("findAllReservations() was called {params: {}}", params)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/reservations").withQuery(Query(params)),
      )
    )
  }

  def getReservationById(id: String): Future[Json] = {
    log.debug("getReservationById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/reservations/$id"),
      )
    )
  }

  def createReservation(body: Json): Future[Json] = {
    log.debug("createReservation() was called {body: {}}", body.noSpaces)

    sendRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"$url/reservations"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateReservation(id: String, body: Json): Future[Json] = {
    log.debug("updateReservation() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/reservations/$id"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateReservationStatus(id: String, body: Json): Future[Json] = {
    log.debug("updateReservationStatus() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/reservations/$id/status"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def deleteReservation(id: String): Future[Json] = {
    log.debug("deleteReservation() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/reservations/$id"),
      )
    )
  }


  def getTableById(mapId: String, id: String): Future[Json] = {
    log.debug("getTableById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/restaurant-map/$mapId/tables/$id"),
      )
    )
  }

  def createTable(mapId: String, body: Json): Future[Json] = {
    log.debug("createTable() was called {body: {}}", body.noSpaces)

    sendRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"$url/restaurant-map/$mapId/tables"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateTable(mapId: String, id: String, body: Json): Future[Json] = {
    log.debug("updateTable() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/restaurant-map/$mapId/tables/$id"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def deleteTable(mapId: String, id: String): Future[Json] = {
    log.debug("deleteTable() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/restaurant-map/$mapId/tables/$id"),
      )
    )
  }


  def getRestaurantMapById(id: String): Future[Json] = {
    log.debug("getRestaurantMapById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/restaurant-maps/$id"),
      )
    )
  }

  def uploadRestaurantMap(fileInfo: FileInfo, byteSource: Source[ByteString, Any]): Future[Json] = {

    log.debug(s"uploadRestaurantMap() was called {fileInfo: $fileInfo}")

    HttpEntity(fileInfo.contentType, byteSource).toStrict(5.minutes).flatMap { httpEntity =>
      val multipartForm = {
        Multipart.FormData(
          Multipart.FormData.BodyPart.Strict(
            "image",
            httpEntity,
            Map("filename" -> fileInfo.getFileName)
          )
        )
      }
      sendRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = Uri(s"$url/reservations"),
          entity =
            multipartForm.toEntity
        )
      )
    }
  }

  def deleteRestaurantMap(id: String): Future[Json] = {
    log.debug("deleteRestaurantMap() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/restaurant-maps/$id"),
      )
    )
  }



}
