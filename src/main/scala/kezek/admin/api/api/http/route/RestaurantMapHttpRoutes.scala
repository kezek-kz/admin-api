package kezek.admin.api.api.http.route

import akka.http.scaladsl.model.StatusCodes
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.Json
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, ExampleObject, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import kezek.admin.api.client.{ReservationCoreHttpClient, RestaurantCoreHttpClient}
import kezek.admin.api.codec.MainCodec
import kezek.admin.api.swagger.UploadMapMultipartRequest
import kezek.admin.api.util.HttpUtil

import javax.ws.rs._
import scala.util.{Failure, Success}

trait RestaurantMapHttpRoutes extends MainCodec {

  val reservationCoreHttpClient: ReservationCoreHttpClient

  def restaurantMapHttpRoutes: Route = {
    pathPrefix("restaurant-maps") {
      concat(
        getTableById,
        updateTable,
        deleteTable,
        addTable,
        getRestaurantMapById,
        deleteRestaurantMap,
        uploadRestaurantMap,
      )
    }
  }

  @POST
  @Operation(
    summary = "Upload restaurant map",
    description = "Uploads restaurant map to s3 and deletes old image",
    method = "POST",
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[UploadMapMultipartRequest]),
          mediaType = "multipart/form-data"
        )
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            examples = Array(new ExampleObject(name = "RestaurantMap", value = "{\n  \"id\": \"restaurant-map\",\n  \"linkToSvg\": \"https://kezek.s3.eu-west-2.amazonaws.com/restaurant-maps/restaurant-map\"\n}")),
            mediaType = "application/json"
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps")
  @Tag(name = "Restaurant Map")
  def uploadRestaurantMap: Route = {
    post {
      pathEndOrSingleSlash {
        fileUpload("map") {
          case (fileInfo, byteSource) => {
            onComplete(reservationCoreHttpClient.uploadRestaurantMap(fileInfo, byteSource)) {
              case Success(result) => complete(result)
              case Failure(exception) => HttpUtil.completeThrowable(exception)
            }
          }
        }
      }
    }
  }

  @GET
  @Operation(
    summary = "Get restaurant map",
    description = "Returns restaurant map",
    method = "GET",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "restaurant-map", required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            examples = Array(new ExampleObject(name = "RestaurantMap", value = "{\n  \"id\": \"restaurant-map\",\n  \"linkToSvg\": \"https://kezek.s3.eu-west-2.amazonaws.com/restaurant-maps/restaurant-map\"\n}"))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{id}")
  @Tag(name = "Restaurant Map")
  def getRestaurantMapById: Route = {
    get {
      path(Segment) { id =>
        onComplete(reservationCoreHttpClient.getRestaurantMapById(id)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @DELETE
  @Operation(
    summary = "Deletes restaurant map",
    description = "Deletes restaurant map",
    method = "DELETE",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "restaurant-map", required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "204",
        description = "OK",
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{id}")
  @Tag(name = "Restaurant Map")
  def deleteRestaurantMap: Route = {
    delete {
      path(Segment) { id =>
        onComplete(reservationCoreHttpClient.deleteRestaurantMap(id)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @GET
  @Operation(
    summary = "Get table by id",
    description = "Returns a full information about table by id",
    method = "GET",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            examples = Array(new ExampleObject(name = "Table", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{mapId}/tables/{id}")
  @Tag(name = "Restaurant Map / Tables")
  def getTableById: Route = {
    get {
      path(Segment / "tables" / Segment) { (mapId, id) =>
        onComplete(reservationCoreHttpClient.getTableById(mapId, id)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @POST
  @Operation(
    summary = "Create table",
    description = "Creates new table",
    method = "POST",
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(
            new ExampleObject(name = "CreateTableDTO", value = "")
          )
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            examples = Array(new ExampleObject(name = "Table", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{mapId}/tables")
  @Tag(name = "Restaurant Map / Tables")
  def addTable: Route = {
    post {
      path("restaurant-maps" / Segment / "tables") { mapId =>
        entity(as[Json]) { body =>
          onComplete(reservationCoreHttpClient.createTable(mapId, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @PUT
  @Operation(
    summary = "Update table",
    description = "Updates table",
    method = "PUT",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(new ExampleObject(name = "UpdateTableDTO", value = ""))
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            examples = Array(new ExampleObject(name = "Table", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{mapId}/tables/{id}")
  @Tag(name = "Restaurant Map / Tables")
  def updateTable: Route = {
    put {
      path("restaurant-maps" / Segment / "tables" / Segment) { (mapId, tableId) =>
        entity(as[Json]) { body =>
          onComplete(reservationCoreHttpClient.updateTable(mapId, tableId, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @DELETE
  @Operation(
    summary = "Deletes table",
    description = "Deletes table",
    method = "DELETE",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "204",
        description = "OK",
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/restaurant-maps/{mapId}/tables/{id}")
  @Tag(name = "Restaurant Map / Tables")
  def deleteTable: Route = {
    delete {
      path("restaurant-maps" / Segment / "tables" / Segment) { (mapId, tableId) =>
        onComplete(reservationCoreHttpClient.deleteTable(mapId, tableId)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

}
