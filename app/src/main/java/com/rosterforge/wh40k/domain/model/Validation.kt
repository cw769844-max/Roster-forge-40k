package com.rosterforge.wh40k.domain.model

data class ValidationResult(
    val isLegal: Boolean,
    val errors: List<ValidationIssue>,
    val warnings: List<ValidationIssue>,
    val pointsUsed: Int,
    val pointsLimit: Int,
    val unitErrorMap: Map<String, List<ValidationIssue>>,
) {
    companion object {
        fun empty(pointsLimit: Int) = ValidationResult(
            isLegal = true,
            errors = emptyList(),
            warnings = emptyList(),
            pointsUsed = 0,
            pointsLimit = pointsLimit,
            unitErrorMap = emptyMap(),
        )
    }
}

data class ValidationIssue(
    val code: ValidationCode,
    val message: String,
    val affectedUnitId: String? = null,    // null = roster-level issue
    val severity: Severity,
)
