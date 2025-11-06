package com.krishna.soslocation


data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isUser: Boolean
)


//data class ChatMessage(
//    val id: String,
//    val text: String,
//    val timestamp: Long,
//    val isSentByMe: Boolean,
//    val status: MessageStatus = MessageStatus.SENT,
//    val isSystemMessage: Boolean = false
//)
//
//



