package kezek.admin.api.domain.dto

import io.circe.Json
import kezek.admin.api.domain.ProductDetail
import org.joda.time.DateTime

case class CreateOrderDTO(orderType: String, // takeaway, in-place,
                          tableDetails: Option[Json],
                          customerId: String,
                          date: DateTime,
                          subtotal: BigDecimal,
                          products: Seq[ProductDetail])
