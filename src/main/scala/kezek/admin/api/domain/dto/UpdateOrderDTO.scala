package kezek.admin.api.domain.dto

import io.circe.Json
import kezek.admin.api.domain.ProductDetail
import org.joda.time.DateTime

case class UpdateOrderDTO(orderType: Option[String] = None, // takeaway, in-place,
                          tableDetails: Option[Json] = None,
                          customerId: Option[String] = None,
                          date: Option[DateTime] = None,
                          subtotal: Option[BigDecimal] = None,
                          products: Option[Seq[ProductDetail]] = None,
                          cancelReason: Option[String] = None,
                          paymentDetails: Option[Json] = None)
