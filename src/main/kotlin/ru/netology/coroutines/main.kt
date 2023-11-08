package ru.netology.coroutines

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.coroutines.dto.Comment
import ru.netology.coroutines.dto.CommentsWithAuthor
import ru.netology.coroutines.dto.Post
import ru.netology.coroutines.dto.PostWithComments
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val client = OkHttpClient.Builder()
	.connectTimeout(30, TimeUnit.SECONDS)
	.addInterceptor(HttpLoggingInterceptor().apply {
		level = HttpLoggingInterceptor.Level.BODY
	})
	.build()

private val gson = Gson()

private const val BASE_URL = "http://127.0.0.1:9999"

data class Author(val id: Long, val name: String)

suspend fun <T> makeCall(url: String, typeToken: TypeToken<T>): T =
	suspendCoroutine { continuation ->
		val request = Request.Builder()
			.url(url)
			.build()

		client.newCall(request).enqueue(object : Callback {
			override fun onResponse(call: Call, response: Response) {
				try {
					continuation.resume(gson.fromJson(response.body?.string(), typeToken.type))
				} catch (e: JsonParseException) {
					continuation.resumeWithException(e)
				}
			}

			override fun onFailure(call: Call, e: IOException) {
				continuation.resumeWithException(e)
			}
		})
	}

suspend fun getPosts(): List<Post> =
	makeCall("$BASE_URL/api/slow/posts", object : TypeToken<List<Post>>() {})

suspend fun getComments(postId: Long): List<Comment> =
	makeCall("$BASE_URL/api/slow/posts/$postId/comments", object : TypeToken<List<Comment>>() {})

suspend fun getAuthor(id: Long): Author? =
	makeCall("$BASE_URL/api/authors/$id", object : TypeToken<Author>() {})

suspend fun main() {
	val posts = getPosts()

	val result = mutableListOf<PostWithComments>()

	for (post in posts) {
		val author = getAuthor(post.authorId)
		val comments = getComments(post.id)

		val commentsWithAuthor = mutableListOf<CommentsWithAuthor>()

		for (comment in comments) {
			val commentAuthor = getAuthor(comment.authorId)
			commentsWithAuthor.add(CommentsWithAuthor(comment, commentAuthor))
		}

		result.add(PostWithComments(post, author, commentsWithAuthor))
	}

	println(result)
	// Execution time: 10 seconds
}



