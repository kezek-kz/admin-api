package kezek.admin.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.{Config, ConfigFactory}
import kezek.admin.api.api.http.HttpServer
import kezek.admin.api.client.{OrderCoreHttpClient, ReservationCoreHttpClient, RestaurantCoreHttpClient}

import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val config: Config = ConfigFactory.load()

  implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](
    Behaviors.empty,
    name = config.getString("akka.actor.system"),
    config
  )

  implicit val classicSystem: akka.actor.ActorSystem = system.classicSystem
  implicit val executionContext: ExecutionContext = classicSystem.dispatchers.lookup("akka.dispatchers.main")

  implicit val restaurantCoreHttpClient: RestaurantCoreHttpClient = new RestaurantCoreHttpClient()
  implicit val orderCoreHttpClient: OrderCoreHttpClient = new OrderCoreHttpClient()
  implicit val reservationCoreHttpClient: ReservationCoreHttpClient = new ReservationCoreHttpClient()

  HttpServer().start()


}
