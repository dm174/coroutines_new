package ru.netology.coroutines.dto

import ru.netology.coroutines.Author

data class PostWithComments(
    val post: Post,
    val author: Author?,
    val comments: List<CommentsWithAuthor>
)