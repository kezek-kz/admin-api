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

class RestaurantCoreHttpClient(implicit val actorSystem: ActorSystem[_],
                               implicit val executionContext: ExecutionContext) extends HttpClient {


  private val config: Config = ConfigFactory.load();
  private val url = config.getString("endpoints.restaurant-core.url")
  private val log: Logger = LoggerFactory.getLogger(getClass.getSimpleName)

  def findAllProducts(params: Map[String, String]): Future[Json] = {
    log.debug("findAllProducts() was called {params: {}}", params)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/products").withQuery(Query(params)),
      )
    )
  }

  def getProductById(id: String): Future[Json] = {
    log.debug("getProductById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/products/$id"),
      )
    )
  }

  def createProduct(body: Json): Future[Json] = {
    log.debug("createProduct() was called {body: {}}", body.noSpaces)

    sendRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"$url/products"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateProduct(id: String, body: Json): Future[Json] = {
    log.debug("updateProduct() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/products/$id"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def deleteProductImage(id: String): Future[Json] = {
    log.debug(s"deleteProductImage() was called {id: $id}")
    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/products/$id/image")
      )
    )
  }

  def uploadProductImage(id: String, fileInfo: FileInfo, byteSource: Source[ByteString, Any]): Future[Json] = {

    log.debug(s"uploadProductImage() was called { id: $id, fileInfo: $fileInfo}")

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
          uri = Uri(s"$url/products/$id/image"),
          entity =
            multipartForm.toEntity
        )
      )
    }
  }


  def deleteProduct(id: String): Future[Json] = {
    log.debug("deleteProduct() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/products/$id"),
      )
    )
  }

  def findAllCategories(params: Map[String, String]): Future[Json] = {
    log.debug("findAllCategories() was called {params: {}}", params)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/categories").withQuery(Query(params)),
      )
    )
  }

  def getCategoryById(id: String): Future[Json] = {
    log.debug("getCategoryById() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(s"$url/categories/$id"),
      )
    )
  }

  def createCategory(body: Json): Future[Json] = {
    log.debug("createCategory() was called {body: {}}", body.noSpaces)

    sendRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"$url/categories"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def updateCategory(id: String, body: Json): Future[Json] = {
    log.debug("updateCategory() was called {id: {}, body: {}}", id, body)

    sendRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = Uri(s"$url/categories/$id"),
        entity = HttpEntity(body.noSpaces)
      )
    )
  }

  def deleteCategory(id: String): Future[Json] = {
    log.debug("deleteCategory() was called {id: {}}", id)

    sendRequest(
      HttpRequest(
        method = HttpMethods.DELETE,
        uri = Uri(s"$url/categories/$id"),
      )
    )
  }

}
