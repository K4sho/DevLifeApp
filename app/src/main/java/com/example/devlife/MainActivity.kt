package com.example.devlife

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.devlife.model.GifPost
import com.example.devlife.model.ResponsePost
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var gifView: ImageView
    private lateinit var gifDesc: TextView
    private lateinit var emptyFeed: TextView
    private lateinit var networkError: LinearLayout

    private var isNetworkError = false
    private var isLoading = false

    private lateinit var history: Storage
    private var tabs = arrayListOf<Storage>()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://developerslife.ru/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gifDesc = findViewById(R.id.textView)
        gifView = findViewById(R.id.imageView)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        networkError = findViewById(R.id.networkError)
        emptyFeed = findViewById(R.id.textViewEmptyFeed)

        tabs.add(Storage("latest"))
        tabs.add(Storage("top"))
        tabs.add(Storage("hot"))
        history = tabs.get(0)

        setState()
        next(history)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                emptyFeed.visibility = View.GONE

                history = tabs.get(tab.position)
                if (history.empty()) {
                    next(history)
                } else {
                    val gifPost: GifPost = history.current()
                    loadImage(gifPost)
                }
                setState()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    /**
     * Метод загрузки гифки из текущего поста в ImageView
     */
    fun loadImage(gifPost: GifPost) {
        isLoading = true
        setState()

        gifView.visibility = View.VISIBLE
        gifDesc.visibility = View.VISIBLE

        gifDesc.text = ""

        Glide.with(gifView.context).load(gifPost.gifURL).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                isNetworkError = true
                setState()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                gifDesc.text = gifPost.description
                isNetworkError = false
                isLoading = false
                setState()
                return false
            }
        }).into(gifView)
    }

    /**
     * Метод для повторного подключения
     */
    fun retry(view: View) {
        isNetworkError = false
        setState()
        next(history)
    }

    /**
     * Хэндлер на кнопку перехода к следующему посту
     */
    fun nextClick(view: View) {
        if (history.isAtEnd()) {
            next(history)
        } else {
            val gifPost: GifPost = history.next()
            loadImage(gifPost)
        }
        setState()
    }

    /**
     * Хэндлер на кнопку возврата
     */
    fun prevClick(view: View) {
        val gifPost: GifPost = history.prev()
        loadImage(gifPost)
        setState()
    }

    /**
     * Метод для установки текущего состояния. Проверяет стоит ли включать/выключать кнопки и состояние подключения
     */
    private fun setState() {
        prevButton.isEnabled = !isLoading && !history.isAtStart()
        nextButton.isEnabled = !isLoading && !history.empty()

        if (isNetworkError) {
            networkError.visibility = View.VISIBLE
        } else {
            networkError.visibility = View.GONE
        }
    }

    /**
     * Метод загрузки списка постов в следующем url'е
     */
    private fun next(history: Storage) {
        isLoading = true

        gifView.visibility = View.GONE
        gifDesc.visibility = View.GONE

        val pageNum: Int = 0.coerceAtLeast(history.size()) / 5
        val category: String = history.getCategory()

        val myCall: Call<ResponsePost> = jsonPlaceholderApi.get_gifPost(category, pageNum)

        // Отправляем GET запрос по текущему url'у
        myCall.enqueue(object : Callback<ResponsePost> {
            override fun onResponse(call: Call<ResponsePost>, response: Response<ResponsePost>) {
                val posts: ResponsePost = response.body()!!

                if (response.isSuccessful) {
                    val gifPosts: List<GifPost> = posts.result
                    if (gifPosts.isNotEmpty()) {
                        for (i in gifPosts.indices) {
                            history.add(gifPosts[i])
                            if (i == 0) {
                                loadImage(history.next())
                                setState()
                            } else {
                                preloadImage(gifPosts[i].gifURL)
                            }
                        }
                    } else {
                        if (history.empty()) {
                            emptyFeed.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "No more items",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponsePost>, t: Throwable) {
                isNetworkError = true
                setState()
            }
        })
    }


    fun preloadImage(url: String) {
        Glide.with(gifView.context).load(url).preload()
    }
}