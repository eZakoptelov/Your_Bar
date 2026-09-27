package com.example.yourbar.cart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.yourbar.cart.domain.CartItem
import com.example.yourbar.databinding.ItemCartBinding

class CartAdapter(
    private val isAdmin: Boolean,                    // ← добавили флаг
    private val onRemove: (String) -> Unit,
    private val onClick: (CartItem) -> Unit,
    private val getSinkPrice: (CartItem) -> Double
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(CartDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCartBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvItemTitle.text = item.displayName

            if (isAdmin) {
                // ── АДМИН: всё как было ──
                showAdminDetails(item)
            } else {
                // ── ОБЫЧНЫЙ ПОЛЬЗОВАТЕЛЬ: только размер, сталь, толщина ──
                showBriefDetails(item)
            }

            binding.btnRemoveItem.setOnClickListener { onRemove(item.id) }
            itemView.setOnClickListener { onClick(item) }
        }

        // Полная версия для админа — без изменений
        private fun showAdminDetails(item: CartItem) {
            val plywoodAreaSqM = (item.widthMm * item.depthMm) / 1_000_000.0

            binding.tvItemDetails.text = buildString {
                append("Сталь: ${item.steelType}")
                append("  |  Толщина: ${item.thicknessMm} мм")
                append("  |  Карманов: ${item.pocketsCount}")
                append("\nТруба 25×25: ${"%.1f".format(item.pipeMeters)} мп")
                append("\nФанера: ${"%.2f".format(plywoodAreaSqM)} м²")
                if (item.faucetHoleCount > 0) append("\nОтверстий для смесителя: ${item.faucetHoleCount} шт")
                if (item.backBoardCount > 0) append("\nЗадний борт: ${item.backBoardCount} шт")
                if (item.adjustableLegCount > 0) append("\nРегулируемые опоры: ${item.adjustableLegCount} шт")
                if (item.solidSinkType != "NONE") {
                    val sinkLabel = when (item.solidSinkType) {
                        "SINK_400x400" -> "400×400"
                        "SINK_400x500" -> "400×500"
                        "SINK_500x500" -> "500×500"
                        "SINK_500x400" -> "500×400"
                        else -> item.solidSinkType
                    }
                    append("\nЦельнотянутая мойка: $sinkLabel мм")
                }
            }
            binding.tvItemDetails.visibility = View.VISIBLE

            binding.tvWeightAisi430.text = "AISI 430: ${"%.1f".format(item.weightAisi430Kg)} кг"
            binding.tvWeightAisi430.visibility = View.VISIBLE

            binding.tvWeightAisi304.text = "AISI 304: ${"%.1f".format(item.weightAisi304Kg)} кг"
            binding.tvWeightAisi304.visibility = View.VISIBLE

            if (item.isBlenderShelfAdded) {
                binding.tvShelfStatus.text = "Полка для блендера: включена"
                binding.tvShelfStatus.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        itemView.context,
                        com.example.yourbar.R.color.color_gray_inactive
                    )
                )
            } else {
                binding.tvShelfStatus.text = "Полка для блендера: не включена"
                binding.tvShelfStatus.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        itemView.context,
                        android.R.color.darker_gray
                    )
                )
            }
            binding.tvShelfStatus.visibility = View.VISIBLE
        }

        // Краткая версия для обычного пользователя
        private fun showBriefDetails(item: CartItem) {
            binding.tvItemDetails.text = buildString {
                append("Размер: ${item.widthMm}×${item.depthMm}×${item.heightMm} мм")
                append("\nСталь: ${item.steelType}")
                append("\nТолщина металла: ${item.thicknessMm} мм")
            }
            binding.tvItemDetails.visibility = View.VISIBLE

            // Прячем всё, что не нужно обычному пользователю
            binding.tvWeightAisi430.visibility = View.GONE
            binding.tvWeightAisi304.visibility = View.GONE
            binding.tvShelfStatus.visibility = View.GONE
        }
    }

    object CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
