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
import kezek.admin.api.client.ReservationCoreHttpClient
import kezek.admin.api.codec.MainCodec
import kezek.admin.api.util.HttpUtil

import javax.ws.rs._
import scala.util.{Failure, Success}

trait ReservationHttpRoutes extends MainCodec {

  val reservationCoreHttpClient: ReservationCoreHttpClient

  def reservationHttpRoutes: Route = {
    pathPrefix("reservations") {
      concat(
        updateReservationStatus,
        updateReservation,
        getReservationById,
        deleteReservation,
        paginateReservations,
        createReservation
      )
    }
  }

  @PUT
  @Operation(
    summary = "Update reservation status",
    description = "Updates reservation's status and appends state to states",
    method = "PUT",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(
            new ExampleObject(name = "CancelDTO", value = "{\n  \"name\": \"ОТМЕНЕН\",\n  \"reason\": \"не успел\"\n}"),
            new ExampleObject(name = "ReservedDTO", value = "{\n  \"name\": \"ЗАБРОНИРОВАНО\",\n  \"paymentDetails\": {}\n}"),
            new ExampleObject(name = "WaitingPaymentDTO", value = "{\n  \"name\": \"ОЖИДАНИЕ ОПЛАТЫ\"}"),
          )
        ),
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
            examples = Array(new ExampleObject(name = "Reservation", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/reservations/{id}/status")
  @Tag(name = "Reservations")
  def updateReservationStatus: Route = {
    put {
      path(Segment / "status") { id =>
        entity(as[Json]) { body =>
          onComplete(reservationCoreHttpClient.updateReservationStatus(id, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @GET
  @Operation(
    summary = "Get reservation list",
    description = "Get filtered and paginated reservation list",
    method = "GET",
    parameters = Array(
      new Parameter(name = "page", in = ParameterIn.QUERY, example = "1"),
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, example = "10"),
      new Parameter(name = "sort", in = ParameterIn.QUERY, example = "")
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Json]),
            mediaType = "application/json",
            examples = Array(new ExampleObject(name = "ReservationListWithTotalDTO", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/reservations")
  @Tag(name = "Reservations")
  def paginateReservations: Route = {
    get {
      pathEndOrSingleSlash {
        parameterMap { params =>
          onComplete(reservationCoreHttpClient.findAllReservations(params)){
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @GET
  @Operation(
    summary = "Get reservation by id",
    description = "Returns a full information about reservation by id",
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
            examples = Array(new ExampleObject(name = "Reservation", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/reservations/{id}")
  @Tag(name = "Reservations")
  def getReservationById: Route = {
    get {
      path(Segment) { id =>
        onComplete(reservationCoreHttpClient.getReservationById(id)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @POST
  @Operation(
    summary = "Create reservation",
    description = "Creates new reservation",
    method = "POST",
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(
            //            new ExampleObject(name = "CreateReservationDTO", value = "")
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
            examples = Array(new ExampleObject(name = "Reservation", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/reservations")
  @Tag(name = "Reservations")
  def createReservation: Route = {
    post {
      pathEndOrSingleSlash {
        entity(as[Json]) { body =>
          onComplete(reservationCoreHttpClient.createReservation(body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @PUT
  @Operation(
    summary = "Update reservation",
    description = "Updates reservation",
    method = "PUT",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(new ExampleObject(name = "UpdateReservationDTO", value = ""))
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
            examples = Array(new ExampleObject(name = "Reservation", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/reservations/{id}")
  @Tag(name = "Reservations")
  def updateReservation: Route = {
    put {
      path(Segment) { id =>
        entity(as[Json]) { body =>
          onComplete(reservationCoreHttpClient.updateReservation(id, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @DELETE
  @Operation(
    summary = "Deletes reservation",
    description = "Deletes reservation",
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
  @Path("/reservations/{id}")
  @Tag(name = "Reservations")
  def deleteReservation: Route = {
    delete {
      path(Segment) { id =>
        onComplete(reservationCoreHttpClient.deleteReservation(id)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

}
