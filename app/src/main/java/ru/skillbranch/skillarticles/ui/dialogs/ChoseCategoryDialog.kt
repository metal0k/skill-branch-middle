package ru.skillbranch.skillarticles.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_category.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

class ChoseCategoryDialog : DialogFragment() {

    companion object {
        private const val SELECTED_CATEGORIES = "SELECTED_CATEGORIES"
    }

    private val viewModel: ArticlesViewModel by activityViewModels()
    private val selectedCategories = mutableListOf<String>()
    private val args: ChoseCategoryDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        selectedCategories.addAll(
            savedInstanceState?.getStringArray(SELECTED_CATEGORIES) ?: args.selectedCategories
        )


        val categories =
            args.categories.toList().map {
                CategoryItem(
                    it.categoryId,
                    it.icon,
                    it.title,
                    it.articlesCount,
                    selectedCategories.contains(it.categoryId)
                )
            }


        val rvCategories = RecyclerView(requireContext()).apply {
            id = R.id.categories_list
            setPaddingOptionally(top = requireContext().dpToIntPx(16))
            layoutManager = LinearLayoutManager(requireContext())
            adapter = object: ListAdapter<CategoryItem, CategoryVH>(CategoryDiffCallback()) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
                    val view = layoutInflater.inflate(R.layout.item_category, parent, false)
                    return CategoryVH(view) {
                        it.checked = !it.checked
                        if (it.checked)
                            selectedCategories.add(it.id)
                        else
                            selectedCategories.remove(it.id)
                    }

                }

                override fun onBindViewHolder(holder: CategoryVH, position: Int) = holder.bind(getItem(position))
            }.also { it.submitList(categories) }
        }

        val adb = AlertDialog.Builder(requireContext())
            .setTitle("Chose category")
            .setView(rvCategories)
            .setPositiveButton("Apply") { _, _ ->
                viewModel.applyCategories(selectedCategories)
            }
            .setNegativeButton("Reset") { _, _ ->
                viewModel.applyCategories(emptyList())
            }

        return adb.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(SELECTED_CATEGORIES, selectedCategories.toTypedArray())
    }
}

data class CategoryItem(
    val id: String,
    val icon: String,
    val title: String,
    val articlesCount: Int,
    var checked: Boolean
)

class CategoryVH(override val containerView: View, val listener: (CategoryItem) -> Unit) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: CategoryItem?) {
        item ?: return

        ch_select.apply {
            isChecked = item.checked
        }

        with(containerView.context) {
            Glide.with(this)
                .load(item.icon)
                .apply(RequestOptions.circleCropTransform())
                .override(iv_icon.width)
                .into(iv_icon)
        }
        tv_category.text = item.title
        tv_count.text = item.articlesCount.toString()
        itemView.setOnClickListener {
            listener.invoke(item)
            ch_select.isChecked = item.checked
        }
    }
}

class CategoryDiffCallback() : DiffUtil.ItemCallback<CategoryItem>() {
    override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
        oldItem == newItem
}