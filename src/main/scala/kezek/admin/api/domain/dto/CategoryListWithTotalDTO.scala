package kezek.admin.api.domain.dto

import kezek.admin.api.domain.Category

case class CategoryListWithTotalDTO(total: Long, collection: Seq[Category])
