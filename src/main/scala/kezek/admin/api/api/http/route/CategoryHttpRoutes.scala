package kezek.admin.api.api.http.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import akka.http.scaladsl.server.Route
import io.circe.Json
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, ExampleObject, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import kezek.admin.api.client.RestaurantCoreHttpClient
import kezek.admin.api.codec.MainCodec
import kezek.admin.api.domain.Category
import kezek.admin.api.domain.dto._
import kezek.admin.api.util.HttpUtil

import javax.ws.rs._
import scala.util.{Failure, Success}

trait CategoryHttpRoutes extends MainCodec {

  val restaurantCoreHttpClient: RestaurantCoreHttpClient

  def categoryHttpRoutes: Route = {
    pathPrefix("categories") {
      concat(
        updateCategory,
        getCategoryById,
        deleteCategory,
        paginateCategories,
        createCategory
      )
    }
  }

  @GET
  @Operation(summary = "Get category list",description = "Get filtered and paginated category list")
  @Parameter(name = "title", in = ParameterIn.QUERY)
  @Parameter(name = "page", in = ParameterIn.QUERY, example = "1")
  @Parameter(name = "pageSize", in = ParameterIn.QUERY, example = "20")
  @Parameter(name = "sort", in = ParameterIn.QUERY, example = "+title")
  @ApiResponse(responseCode = "200",description = "OK",content = Array(new Content(schema = new Schema(implementation = classOf[CategoryListWithTotalDTO]))))
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/categories")
  @Tag(name = "Categories")
  def paginateCategories: Route = {
    get {
      pathEndOrSingleSlash {
        parameterMap { params => {
            onComplete {
              restaurantCoreHttpClient.findAllCategories(params)
            } {
              case Success(result) => complete(result)
              case Failure(exception) => HttpUtil.completeThrowable(exception)
            }
          }
        }
      }
    }
  }

  @GET
  @Operation(summary = "Get category by slug",description = "Returns category by slug")
  @Parameter(name = "slug", in = ParameterIn.PATH, example = "", required = true)
  @ApiResponse(responseCode = "200",description = "OK",content = Array(new Content(schema = new Schema(implementation = classOf[Category]))))
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/categories/{slug}")
  @Tag(name = "Categories")
  def getCategoryById: Route = {
    get {
      path(Segment) { slug =>
        onComplete(restaurantCoreHttpClient.getCategoryById(slug)) {
          case Success(result) => complete(result)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

  @POST
  @Operation(summary = "Create category",description = "Creates new category")
  @RequestBody(
    content = Array(new Content(schema = new Schema(implementation = classOf[CreateCategoryDTO]),
      examples = Array(
        new ExampleObject(name = "CreateCategoryDTO", value = "{\n  \"title\": \"Fruits & Vegetables\",\n  \"slug\": \"fruits-and-vegetables\"\n}"),
        new ExampleObject(name = "Create Many catergories", value = "[{\n  \"title\": \"Fruits & Vegetables\",\n  \"slug\": \"fruits-and-vegetables\"\n}]")
      )
    )
    ),
    required = true
  )
  @ApiResponse(responseCode = "200",description = "OK",content = Array(new Content(schema = new Schema(implementation = classOf[Seq[Category]]))))
  @ApiResponse(responseCode = "200",description = "OK",content = Array(new Content(schema = new Schema(implementation = classOf[Category]))))
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/categories")
  @Tag(name = "Categories")
  def createCategory: Route = {
    post {
      pathEndOrSingleSlash {
        entity(as[Json]) { body =>
          onComplete(restaurantCoreHttpClient.createCategory(body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @PUT
  @Operation(summary = "Update category",description = "Updates category")
  @Parameter(name = "slug", in = ParameterIn.PATH, required = true)
  @RequestBody(required = true,content = Array(new Content(schema = new Schema(implementation = classOf[UpdateCategoryDTO]))))
  @ApiResponse(responseCode = "200",description = "OK",content = Array(new Content(schema = new Schema(implementation = classOf[Category]))))
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/categories/{slug}")
  @Tag(name = "Categories")
  def updateCategory: Route = {
    put {
      path(Segment) { slug =>
        entity(as[Json]) { body =>
          onComplete(restaurantCoreHttpClient.updateCategory(slug, body)) {
            case Success(result) => complete(result)
            case Failure(exception) => HttpUtil.completeThrowable(exception)
          }
        }
      }
    }
  }

  @DELETE
  @Operation(summary = "Delete category", description = "Deletes category")
  @ApiResponse(responseCode = "204",description = "NoContent")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/categories/{slug}")
  @Tag(name = "Categories")
  def deleteCategory: Route = {
    delete {
      path(Segment) { slug =>
        onComplete(restaurantCoreHttpClient.deleteCategory(slug)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(exception) => HttpUtil.completeThrowable(exception)
        }
      }
    }
  }

}
