package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import java.lang.Thread.sleep

object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))

    fun allBookmarkArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticleStrategy.BookmarkArticles(::findBookmarkArticlesByRange))

    fun searchArticles(searchQuery: String) =
        ArticlesDataFactory(ArticleStrategy.SearchArticle(::searchArticlesByTitle, searchQuery))

    fun searchBookmarkArticles(searchQuery: String) =
        ArticlesDataFactory(ArticleStrategy.SearchBookmark(::searchBookmarkArticlesByTitle, searchQuery))

    private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
        .drop(start)
        .take(size)

    private fun findBookmarkArticlesByRange(start: Int, size: Int) = local.localArticleItems
        .asSequence()
        .filter { it.isBookmark }
        .drop(start)
        .take(size)
        .toList()

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle:String) = local.localArticleItems
        .asSequence()
        .filter { it.title.contains(queryTitle, true) }
        .drop(start)
        .take(size)
        .toList()

    private fun searchBookmarkArticlesByTitle(start: Int, size: Int, queryTitle:String) = local.localArticleItems
        .asSequence()
        .filter { it.isBookmark && it.title.contains(queryTitle, true) }
        .drop(start)
        .take(size)
        .toList()

    fun loadArticlesFromNetwork(start: Int, size: Int):List<ArticleItemData> = network.networkArticleItems
            .drop(start)
            .take(size)
            .apply { sleep(500) }

    fun insertArticlesToDb(articles:List<ArticleItemData>){
        local.localArticleItems.addAll(articles)
            .apply { sleep(100) }
    }

    fun updateBookmark(id: String, isChecked: Boolean) {
        Log.e("ArticlesRepository", "updateBookmark $id: $isChecked")
        val index = local.localArticleItems.indexOfFirst { it.id == id  };
        if (index != -1) {
            local.localArticleItems[index] = local.localArticleItems[index].copy(isBookmark = isChecked);
        }
    }
}

class ArticlesDataFactory(val strategy: ArticleStrategy) :
    DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)
}


class ArticleDataSource(private val strategy: ArticleStrategy) : PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        Log.e(
            "ArticlesRepository",
            "loadInitial: start > ${params.requestedStartPosition}  size > ${params.requestedLoadSize} resultSize > ${result.size}"
        );
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        Log.e(
            "ArticlesRepository",
            "loadRange: start > ${params.startPosition}  size > ${params.loadSize} resultSize > ${result.size}"
        );
        callback.onResult(result)
    }
}

sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }

    class SearchArticle(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }

    class BookmarkArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)

    }


    class SearchBookmark(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }
}