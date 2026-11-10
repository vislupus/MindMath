package com.vislupus.mindmath.core

import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.methods.AddByRounding
import com.vislupus.mindmath.core.methods.AddLeftToRight
import com.vislupus.mindmath.core.methods.CrissCross
import com.vislupus.mindmath.core.methods.DifferenceOfSquares
import com.vislupus.mindmath.core.methods.DivideBy5Family
import com.vislupus.mindmath.core.methods.DivideByChunks
import com.vislupus.mindmath.core.methods.DivideByFactors
import com.vislupus.mindmath.core.methods.DoubleAndHalve
import com.vislupus.mindmath.core.methods.MultiplyBy11
import com.vislupus.mindmath.core.methods.MultiplyBy5Family
import com.vislupus.mindmath.core.methods.MultiplyByNines
import com.vislupus.mindmath.core.methods.MultiplyLeftToRight
import com.vislupus.mindmath.core.methods.MultiplySplit
import com.vislupus.mindmath.core.methods.NearHundred
import com.vislupus.mindmath.core.methods.PercentBlocks
import com.vislupus.mindmath.core.methods.PercentChange
import com.vislupus.mindmath.core.methods.PercentFlip
import com.vislupus.mindmath.core.methods.ShortDivision
import com.vislupus.mindmath.core.methods.SquareAnyNumber
import com.vislupus.mindmath.core.methods.SquaresEndingIn5
import com.vislupus.mindmath.core.methods.SubtractByRounding
import com.vislupus.mindmath.core.methods.SubtractComplement
import com.vislupus.mindmath.core.methods.WhatPercent

/** Central list of all mental math methods. */
object Methods {

    /** Every method the app knows, whether enabled or not. */
    val all: List<MathMethod> = listOf(
        AddLeftToRight, AddByRounding, SubtractByRounding, SubtractComplement,
        MultiplyBy11, MultiplyBy5Family, MultiplyByNines, DoubleAndHalve, MultiplyLeftToRight,
        MultiplySplit, CrissCross, NearHundred, DifferenceOfSquares,
        SquaresEndingIn5, SquareAnyNumber,
        ShortDivision, DivideBy5Family, DivideByFactors, DivideByChunks,
        PercentBlocks, PercentFlip, PercentChange, WhatPercent,
    )

    private val byId: Map<String, MathMethod> = all.associateBy { it.id }

    /** Methods shown in the app, in the order given by AppConfig.ENABLED_METHODS. */
    val enabled: List<MathMethod> = AppConfig.ENABLED_METHODS.mapNotNull { byId[it] }.ifEmpty { all }

    /** Categories that have at least one enabled method, in display order. */
    val categories: List<Category> = Category.entries.filter { category -> enabled.any { it.category == category } }

    fun get(id: String): MathMethod? = byId[id]

    fun inCategory(category: Category): List<MathMethod> = enabled.filter { it.category == category }
}
