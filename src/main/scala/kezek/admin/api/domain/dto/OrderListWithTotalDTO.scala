package kezek.admin.api.domain.dto

import kezek.admin.api.domain.Order

case class OrderListWithTotalDTO(total: Long, collection: Seq[Order])
