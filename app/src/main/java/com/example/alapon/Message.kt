package com.example.alapon

interface Message {
    val messageID: String
    val senderID: String
    val receiverID: String
}

data class TextMessage(
    val textMessage: String? = null,
    override var messageID: String = "",
    override val senderID: String = "",
    override val receiverID: String = ""
) : Message

data class MessageWithImage(
    val imageMessage: String? = null,
    override val messageID: String,
    override val senderID: String,
    override val receiverID: String
) : Message