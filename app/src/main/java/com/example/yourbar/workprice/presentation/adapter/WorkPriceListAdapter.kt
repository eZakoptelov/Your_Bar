package com.example.yourbar.workprice.presentation.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourbar.databinding.ItemWorkPriceBinding
import com.example.yourbar.workprice.data.WorkPriceEntity

class WorkPriceListAdapter(
    private val onPriceChanged: (WorkPriceEntity) -> Unit
) : ListAdapter<WorkPriceEntity, WorkPriceListAdapter.WorkPriceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkPriceViewHolder {
        val binding = ItemWorkPriceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkPriceViewHolder(binding, onPriceChanged)
    }

    override fun onBindViewHolder(holder: WorkPriceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkPriceViewHolder(
        private val binding: ItemWorkPriceBinding,
        private val onPriceChanged: (WorkPriceEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentEntity: WorkPriceEntity? = null
        private var textWatcher: TextWatcher? = null

        fun bind(entity: WorkPriceEntity) {
            currentEntity = entity

            binding.tvWorkTitle.text = entity.title
            binding.tvWorkUnit.text = entity.unit

            // Удаляем старый слушатель
            textWatcher?.let { binding.etWorkPrice.removeTextChangedListener(it) }

            // Ставим текст, только если отличается — курсор не сбросится
            val currentText = binding.etWorkPrice.text.toString()
            val newText = if (entity.price > 0) entity.price.toString() else ""
            if (currentText != newText) {
                binding.etWorkPrice.setText(newText)
                binding.etWorkPrice.setSelection(binding.etWorkPrice.text.length)
            }

            // Возвращаем слушатель
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    currentEntity?.let { entity ->
                        val price = s?.toString()?.toIntOrNull() ?: 0
                        if (entity.price != price) {
                            entity.price = price
                            onPriceChanged(entity)
                        }
                    }
                }
            }
            binding.etWorkPrice.addTextChangedListener(textWatcher)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WorkPriceEntity>() {
        override fun areItemsTheSame(oldItem: WorkPriceEntity, newItem: WorkPriceEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WorkPriceEntity, newItem: WorkPriceEntity): Boolean =
            oldItem == newItem
    }
}
