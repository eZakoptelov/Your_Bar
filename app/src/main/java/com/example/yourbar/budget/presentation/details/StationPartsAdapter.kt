package com.example.yourbar.budget.presentation.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourbar.budget.domain.calculator.models.StationPart
import com.example.yourbar.databinding.ItemStationPartBinding

class StationPartsAdapter :
    ListAdapter<StationPart, StationPartsAdapter.ViewHolder>(StationPartDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStationPartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemStationPartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(part: StationPart) {
            binding.tvPartTitle.text = part.title

            val isSimpleItem = part.title == "Отверстие для смесителя"
                    || part.title == "Задний борт"
                    || part.title == "Регулируемая опора"

            if (isSimpleItem) {
                binding.tvPartDimensions.visibility = View.GONE
            } else {
                binding.tvPartDimensions.visibility = View.VISIBLE
                binding.tvPartDimensions.text = "Размеры: ${part.dimensions}"
            }

            binding.tvPartQuantity.visibility = View.VISIBLE
            binding.tvPartQuantity.text = "Количество: ${part.quantity} шт"

            if (part.steelType == "—") {
                binding.tvPartMaterial.visibility = View.GONE
                binding.tvPartWeight.visibility = View.GONE
            } else {
                binding.tvPartMaterial.visibility = View.VISIBLE
                binding.tvPartMaterial.text =
                    "Сталь: ${part.steelType}, толщина ${part.thicknessMm} мм"

                if (part.weightKg > 0.0) {
                    binding.tvPartWeight.visibility = View.VISIBLE
                    binding.tvPartWeight.text = "Вес: ${"%.2f".format(part.weightKg)} кг"
                } else {
                    binding.tvPartWeight.visibility = View.GONE
                }
            }
        }
    }

        object StationPartDiffCallback : DiffUtil.ItemCallback<StationPart>() {
        override fun areItemsTheSame(oldItem: StationPart, newItem: StationPart) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: StationPart, newItem: StationPart) =
            oldItem == newItem
    }
}
