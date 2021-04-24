package kezek.admin.api.client

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.Json
import io.circe.parser.parse
import kezek.admin.api.exception.ApiException
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait HttpClient {

  implicit val actorSystem: ActorSystem[_]
  implicit val executionContext: ExecutionContext

  private val log: Logger = LoggerFactory.getLogger(getClass.getSimpleName)

  def sendRequest(request: HttpRequest): Future[Json] = {
    Http().singleRequest(request).flatMap {
      response => {
        Unmarshal(response.entity).to[String].map { jsonString =>
          log.debug(s"sendRequest() received payload: $jsonString")
          parse(jsonString) match {
            case Right(json) => json
            case Left(error) =>
              log.error("sendRequest() failed to parse json: {}", error)
              throw ApiException(StatusCodes.ServiceUnavailable, "Can't parse json")
          }
        }
      }
    }
  }

}
