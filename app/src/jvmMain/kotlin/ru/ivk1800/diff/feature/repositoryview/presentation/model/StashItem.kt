package ru.ivk1800.diff.feature.repositoryview.presentation.model

data class StashItem(val id: Id, val message: String) {
    data class Id(val value: Int)
}
