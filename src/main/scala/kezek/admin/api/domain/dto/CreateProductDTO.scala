package kezek.admin.api.domain.dto

case class CreateProductDTO(title: String,
                            slug: String,
                            unit: Option[String],
                            firstPrice: BigDecimal,
                            secondPrice: Option[BigDecimal],
                            description: Option[String],
                            categories: Seq[String])
