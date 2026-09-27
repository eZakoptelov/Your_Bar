package com.example.yourbar.cart.data.mapper

import com.example.yourbar.cart.data.entity.CartEntity
import com.example.yourbar.cart.domain.CartItem

fun CartEntity.toDomain(): CartItem = CartItem(
    id = id,
    name = name,
    widthMm = widthMm,
    depthMm = depthMm,
    heightMm = heightMm,
    steelType = steelType,
    thicknessMm = thicknessMm,
    pocketsCount = pocketsCount,
    totalWeightKg = totalWeightKg,
    weightAisi304Kg = weightAisi304Kg,
    weightAisi430Kg = weightAisi430Kg,
    pipeMeters = pipeMeters,
    isBlenderShelfAdded = isBlenderShelfAdded,
    blenderShelfWidthMm = blenderShelfWidthMm,
    insulationAreaSqM = insulationAreaSqM,
    faucetHoleCount = faucetHoleCount,
    faucetHolePricePerUnit = faucetHolePricePerUnit,
    backBoardCount = backBoardCount,
    backBoardPricePerUnit = backBoardPricePerUnit,
    adjustableLegCount = adjustableLegCount,
    adjustableLegPricePerUnit = adjustableLegPricePerUnit,
    pocketHeightMm = pocketHeightMm,
    solidSinkType = solidSinkType,
    solidSinkPrice = solidSinkPrice
)

fun CartItem.toEntity(): CartEntity = CartEntity(
    id = id,
    name = name,
    widthMm = widthMm,
    depthMm = depthMm,
    heightMm = heightMm,
    steelType = steelType,
    thicknessMm = thicknessMm,
    pocketsCount = pocketsCount,
    totalWeightKg = totalWeightKg,
    weightAisi304Kg = weightAisi304Kg,
    weightAisi430Kg = weightAisi430Kg,
    pipeMeters = pipeMeters,
    isBlenderShelfAdded = isBlenderShelfAdded,
    blenderShelfWidthMm = blenderShelfWidthMm,
    insulationAreaSqM = insulationAreaSqM,
    faucetHoleCount = faucetHoleCount,
    faucetHolePricePerUnit = faucetHolePricePerUnit,
    backBoardCount = backBoardCount,
    backBoardPricePerUnit = backBoardPricePerUnit,
    adjustableLegCount = adjustableLegCount,
    adjustableLegPricePerUnit = adjustableLegPricePerUnit,
    pocketHeightMm = pocketHeightMm,
    solidSinkType = solidSinkType,
    solidSinkPrice = solidSinkPrice
)
