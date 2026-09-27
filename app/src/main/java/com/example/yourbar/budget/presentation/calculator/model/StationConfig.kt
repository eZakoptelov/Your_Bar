package com.example.yourbar.budget.presentation.calculator.model

import com.example.yourbar.budget.domain.calculator.models.SolidSinkType

data class StationConfig(
    var pocketCount: Int = 0,
    var pocketHeightChoice: Int = 0,
    var blenderShelfWidthMm: Int = 0,
    var solidSinkType: SolidSinkType = SolidSinkType.NONE
) {
    val pocketHeightMm: Int get() = if (pocketHeightChoice == 0) 260 else 210
    val isShelfAdded: Boolean get() = blenderShelfWidthMm > 0
}
