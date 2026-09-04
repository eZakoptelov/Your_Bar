package com.example.yourbar.cart.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.yourbar.cart.domain.CartItem
import com.example.yourbar.databinding.ItemCartBinding

class CartAdapter(
    private val onRemove: (String) -> Unit,
    private val onClick: (CartItem) -> Unit
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

            binding.tvItemDetails.text = buildString {
                append("Сталь: ${item.steelType}")
                append("  |  Толщина: ${item.thicknessMm} мм")
                append("  |  Карманов: ${item.pocketsCount}")
                append("\nТруба 25×25: ${"%.1f".format(item.pipeMeters)} мп")
            }

            // Сначала AISI 430, потом AISI 304
            binding.tvWeightAisi430.text = "AISI 430: ${"%.1f".format(item.weightAisi430Kg)} кг"
            binding.tvWeightAisi304.text = "AISI 304: ${"%.1f".format(item.weightAisi304Kg)} кг"

            binding.btnRemoveItem.setOnClickListener { onRemove(item.id) }
            itemView.setOnClickListener { onClick(item) }
        }
    }


    object CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
