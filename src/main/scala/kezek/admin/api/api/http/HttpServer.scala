package kezek.admin.api.api.http

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.config.{Config, ConfigFactory}
import kezek.admin.api.client.{OrderCoreHttpClient, ReservationCoreHttpClient, RestaurantCoreHttpClient}
import kezek.admin.api.swagger.{SwaggerService, SwaggerSite}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

case class HttpServer()(implicit val actorSystem: ActorSystem[_],
                        implicit val executionContext: ExecutionContext,
                        implicit val orderCoreHttpClient: OrderCoreHttpClient,
                        implicit val restaurantCoreHttpClient: RestaurantCoreHttpClient,
                        implicit val reservationCoreHttpClient: ReservationCoreHttpClient)
  extends HttpRoutes with SwaggerSite {

  implicit val config: Config = ConfigFactory.load()

  private val shutdown = CoordinatedShutdown(actorSystem)
  private val log: Logger = LoggerFactory.getLogger(getClass.getSimpleName)
  private val port = config.getInt("http-server.port")
  private val interface = config.getString("http-server.interface")

  def start(): Unit = {
    log.debug(s"start() PORT: '$port'")
    Http()
      .newServerAt(interface, port)
      .bind(
        cors(CorsSettings(config)) { concat (routes, swaggerSiteRoute, new SwaggerService().routes) }
      )
      .onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          actorSystem.log.info("admin-api serving at http://{}:{}/", address.getHostString, address.getPort)

          shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "server graceful terminating") { () =>
            binding.terminate(10.seconds).map { _ =>
              log.info("admin-api http://{}:{}/ graceful shutdown completed", address.getHostString, address.getPort)
              Done
            }
          }
        case Failure(ex) =>
          actorSystem.log.error("Failed to bind HTTP endpoint, terminating system", ex)
          actorSystem.terminate()
      }
  }

}
