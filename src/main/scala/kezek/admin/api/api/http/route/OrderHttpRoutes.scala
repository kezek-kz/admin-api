package kezek.admin.api.api.http.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Json
import io.circe.generic.auto._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, ExampleObject, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import kezek.admin.api.client.OrderCoreHttpClient
import kezek.admin.api.codec.MainCodec
import kezek.admin.api.util.HttpUtil

import javax.ws.rs._
import scala.util.{Failure, Success}

trait OrderHttpRoutes extends MainCodec {

  val orderCoreHttpClient: OrderCoreHttpClient

  def orderHttpRoutes: Route = {
    pathPrefix("orders") {
      concat(
        updateOrderStatus,
        updateOrder,
        getOrderById,
        deleteOrder,
        findAllOrders,
        createOrder
      )
    }
  }

  @PUT
  @Operation(
    summary = "Update order status",
    description = "Updates order's status and appends state to states",
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
            new ExampleObject(name = "ApprovedDTO", value = "{\n  \"name\": \"ПОДТВЕРЖДЕН\"\n}"),
            new ExampleObject(name = "RejectedDTO", value = "{\n  \"name\": \"ОТКАЗОНО\"\n  \"reason\": \"Some reason\"\n}"),
            new ExampleObject(name = "PaidDTO", value = "{\n  \"name\": \"ОПЛАЧЕН\",\n  \"paymentDetails\": {}\n}"),
            new ExampleObject(name = "PreparingDTO", value = "{\n  \"name\": \"ГОТОВИТЬСЯ\"\n}"),
            new ExampleObject(name = "CompletedDTO", value = "{\n  \"name\": \"ГОТОВО\"\n}"),
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
            examples = Array(new ExampleObject(name = "Order", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders/{id}/status")
  @Tag(name = "Orders")
  def updateOrderStatus: Route = {
    put {
      path(Segment / "status") { id =>
        entity(as[Json]) { body =>
          onComplete(orderCoreHttpClient.updateOrderStatus(id, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
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
            examples = Array(new ExampleObject(name = "Json", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @Path("/orders")
  @Tag(name = "Orders")
  def findAllOrders: Route = {
    get {
      pathEndOrSingleSlash {
        parameterMap { params =>
            onComplete(orderCoreHttpClient.findAllOrders(params)){
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
    description = "Returns order by id",
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
            examples = Array(new ExampleObject(name = "Order", value = ""))
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
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(
//            new ExampleObject(name = "Json", value = "")
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
            examples = Array(new ExampleObject(name = "Order", value = ""))
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
      new Parameter(name = "id", in = ParameterIn.PATH, example = "", required = true),
    ),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[Json]),
          mediaType = "application/json",
          examples = Array(new ExampleObject(name = "Json", value = ""))
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
            examples = Array(new ExampleObject(name = "Order", value = ""))
          )
        )
      ),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
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
  @Path("/orders/{id}")
  @Tag(name = "Orders")
  def deleteOrder: Route = {
    delete {
      path(Segment) { id =>
        onComplete(orderCoreHttpClient.deleteOrder(id)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

}
