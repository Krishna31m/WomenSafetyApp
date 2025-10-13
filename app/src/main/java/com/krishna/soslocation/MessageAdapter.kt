package com.krishna.soslocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM = 3

        // Make formatTime accessible
        fun formatTime(timestamp: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp

            val now = Calendar.getInstance()

            return when {
                isSameDay(calendar, now) -> {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
                isYesterday(calendar, now) -> {
                    "Yesterday ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
                }
                else -> {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            return cal1.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isSystemMessage -> VIEW_TYPE_SYSTEM
            messages[position].isSentByMe -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_SYSTEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_system, parent, false)
                SystemMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // ViewHolder for sent messages
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessageSent)
        private val timeText: TextView = itemView.findViewById(R.id.textTimeSent)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
            timeText.text = MessageAdapter.formatTime(message.timestamp)
        }
    }

    // ViewHolder for received messages
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessageReceived)
        private val timeText: TextView = itemView.findViewById(R.id.textTimeReceived)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
            timeText.text = MessageAdapter.formatTime(message.timestamp)
        }
    }

    // ViewHolder for system messages
    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textSystemMessage)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
        }
    }
}