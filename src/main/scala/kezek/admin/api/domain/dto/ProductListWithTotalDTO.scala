package kezek.admin.api.domain.dto

case class ProductListWithTotalDTO(total: Long, collection: Seq[ProductDTO])
