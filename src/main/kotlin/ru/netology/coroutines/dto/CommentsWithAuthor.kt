package ru.netology.coroutines.dto

import ru.netology.coroutines.Author

data class CommentsWithAuthor(
    val comment: Comment,
    val author: Author?
)