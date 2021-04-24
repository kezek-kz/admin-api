package kezek.admin.api.api.http.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Json
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, ExampleObject, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import kezek.admin.api.client.OrderCoreHttpClient
import kezek.admin.api.codec.MainCodec
import kezek.admin.api.domain.Order
import kezek.admin.api.util.HttpUtil
import kezek.admin.api.domain.dto.{CreateOrderDTO, OrderListWithTotalDTO, UpdateOrderDTO}

import javax.ws.rs._
import scala.util.{Failure, Success}

trait OrderHttpRoutes extends MainCodec {

  val orderCoreHttpClient: OrderCoreHttpClient

  def orderHttpRoutes: Route = {
    pathPrefix("orders") {
      concat(
        handleOrderEvent,
        updateOrder,
        getOrderById,
        deleteOrder,
        paginateOrders,
        createOrder
      )
    }
  }

  @POST
  @Operation(summary = "Handle order event")
  @RequestBody(
    required = true,
    content = Array(
      new Content(
        schema = new Schema(implementation = classOf[Json]),
        mediaType = "application/json",
        examples = Array(
          new ExampleObject(name = "Cancel", value = "{\n  \"reason\": \"Some cancel reason\"\n}"),
          new ExampleObject(name = "Checkout", value = "{ \"some\": \"json\"}"),
          new ExampleObject(name = "Empty", value = "{}")
        )
      )
    )
  )
  @Parameter(name = "id", in = ParameterIn.PATH, required = true)
  @Parameter(
    name = "event",
    in = ParameterIn.PATH,
    description = "Events: checkout, cancel, cook, cooked, taken",
    required = true
  )
  @ApiResponse(responseCode = "200", description = "OK", content = Array(new Content(schema = new Schema(implementation = classOf[Order]))))
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/orders/{id}/{event}")
  @Tag(name = "Orders")
  def handleOrderEvent: Route = {
    post {
      path(Segment / Segment) { (id, event) =>
        concat (
          entity(as[Json]) { body =>
            onComplete(orderCoreHttpClient.handleOrderEvent(id, event, body)) {
              case Success(result) => complete(result)
              case Failure(exception) => HttpUtil.completeThrowable(exception)
            }
          }
        )
      }
    }
  }

  @GET
  @Operation(
    summary = "Get order list",
    description = "Get filtered and paginated order list",
    method = "GET",
    parameters = Array(
      new Parameter(name = "page", in = ParameterIn.QUERY, example = "1"),
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, example = "10"),
      new Parameter(name = "sort", in = ParameterIn.QUERY, example = "-createdAt")
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[OrderListWithTotalDTO]),
            mediaType = "application/json"
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders")
  @Tag(name = "Orders")
  def paginateOrders: Route = {
    get {
      pathEndOrSingleSlash {
        parameterMap { params => 
          onComplete(orderCoreHttpClient.findAllOrders(params)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @GET
  @Operation(
    summary = "Get order by id",
    description = "Returns a full information about order by id",
    method = "GET",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Order])
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders/{id}")
  @Tag(name = "Orders")
  def getOrderById: Route = {
    get {
      path(Segment) { id =>
        onComplete(orderCoreHttpClient.getOrderById(id)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @POST
  @Operation(
    summary = "Create order",
    description = "Creates new order",
    method = "POST",
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[CreateOrderDTO]),
          mediaType = "application/json",
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
            schema = new Schema(implementation = classOf[Order])
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders")
  @Tag(name = "Orders")
  def createOrder: Route = {
    post {
      pathEndOrSingleSlash {
        entity(as[Json]) { body =>
          onComplete(orderCoreHttpClient.createOrder(body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @PUT
  @Operation(
    summary = "Update order",
    description = "Updates order",
    method = "PUT",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[Order]),
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[UpdateOrderDTO]), mediaType = "application/json")), required = true)
  @Path("/orders/{id}")
  @Tag(name = "Orders")
  def updateOrder: Route = {
    put {
      path(Segment) { id =>
        entity(as[Json]) { body =>
          onComplete(orderCoreHttpClient.updateOrder(id, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @DELETE
  @Operation(
    summary = "Deletes order",
    description = "Deletes order",
    method = "DELETE",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, required = true),
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "204",
        description = "OK",
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders/{id}")
  @Tag(name = "Orders")
  def deleteOrder: Route = {
    delete {
      path(Segment) { id =>
        onComplete(orderCoreHttpClient.deleteOrder(id)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }


}
