package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import com.example.bluetoothchattingsystem.theme.AlertRed
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.SoftWhite
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatViewModel: ChatViewModel,
    peerAddress: String,
    peerName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sync Viewmodel target peer and start auto-reconnection
    LaunchedEffect(peerAddress) {
        chatViewModel.setPeerAddress(peerAddress)
        chatViewModel.monitorAndReconnect(peerAddress, peerName)
    }

    val messages by chatViewModel.messages.collectAsState()
    val typedText by chatViewModel.typedText.collectAsState()
    val connectedDevice by chatViewModel.connectedDevice.collectAsState()

    val isConnected = connectedDevice != null && connectedDevice!!.address == peerAddress && connectedDevice!!.connectionState == ConnectionState.CONNECTED
    val isConnecting = connectedDevice != null && connectedDevice!!.address == peerAddress && connectedDevice!!.connectionState == ConnectionState.CONNECTING

    val listState = rememberLazyListState()

    // Dialog state for edit/delete message actions
    var showEditDialog by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editDialogText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingMessage by remember { mutableStateOf<MessageEntity?>(null) }

    // Scroll to last message when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with initials
                        val initials = peerName.take(2).uppercase()
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) TheMint.copy(alpha = 0.15f) else IceLatte)
                                .border(1.dp, if (isConnected) TheMint else LatteDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) TheMint else NearBlack,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = peerName,
                                color = NearBlack,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            // Connection State Info Subtitle
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) TheMint else if (isConnecting) TheMint else AlertRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isConnected) "Connected" else if (isConnecting) "Connecting..." else "Disconnected",
                                    color = if (isConnected) TheMint else if (isConnecting) TheMint else LatteDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = NearBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IceLatte
                )
            )
        },
        containerColor = SoftWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Reconnecting Warning Banner (Visible when disconnected or connecting)
                AnimatedVisibility(
                    visible = !isConnected,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF2D6))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    color = Color(0xFFB25E00),
                                    strokeWidth = 1.5.dp,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnecting) "Re-establishing link..." else "Link dropped. Auto-reconnecting...",
                                color = Color(0xFFB25E00),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Message History Thread list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            onEditClick = { msg ->
                                editingMessage = msg
                                editDialogText = msg.messageText
                                showEditDialog = true
                            },
                            onDeleteClick = { msg ->
                                deletingMessage = msg
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Chat Input Bar (only active when connected)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IceLatte)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedText,
                        onValueChange = { chatViewModel.updateTypedText(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...", color = NearBlack.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = LatteDark,
                            unfocusedBorderColor = LatteDark,
                            focusedTextColor = NearBlack,
                            unfocusedTextColor = NearBlack
                        ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            chatViewModel.sendCurrentMessage()
                        }),
                        enabled = isConnected
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { chatViewModel.sendCurrentMessage() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isConnected && typedText.isNotBlank()) TheMint else LatteDark),
                        enabled = isConnected && typedText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Packet",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Edit Alert Dialog
            if (showEditDialog && editingMessage != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showEditDialog = false 
                        editingMessage = null
                    },
                    title = { Text("Edit Message", fontWeight = FontWeight.Bold, color = NearBlack) },
                    text = {
                        OutlinedTextField(
                            value = editDialogText,
                            onValueChange = { editDialogText = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TheMint,
                                unfocusedBorderColor = LatteDark,
                                focusedTextColor = NearBlack,
                                unfocusedTextColor = NearBlack
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                editingMessage?.let { msg ->
                                    chatViewModel.editMessage(msg.id, editDialogText)
                                }
                                showEditDialog = false
                                editingMessage = null
                            }
                        ) {
                            Text("Save", color = TheMint, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showEditDialog = false 
                                editingMessage = null
                            }
                        ) {
                            Text("Cancel", color = LatteDark)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog && deletingMessage != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteDialog = false 
                        deletingMessage = null
                    },
                    title = { Text("Delete Message", fontWeight = FontWeight.Bold, color = NearBlack) },
                    text = { Text("Are you sure you want to delete this message? This action is permanent and cannot be undone locally.", color = NearBlack.copy(alpha = 0.8f)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deletingMessage?.let { msg ->
                                    chatViewModel.deleteMessage(msg.id)
                                }
                                showDeleteDialog = false
                                deletingMessage = null
                            }
                        ) {
                            Text("Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showDeleteDialog = false 
                                deletingMessage = null
                            }
                        ) {
                            Text("Cancel", color = LatteDark)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    onEditClick: (MessageEntity) -> Unit,
    onDeleteClick: (MessageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSent = message.isSent
    val bubbleShape = if (isSent) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isSent) TheMint else IceLatte
    val textColor = if (isSent) Color.White else NearBlack
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    var showContextDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .combinedClickable(
                            onClick = { /* normal click - no action */ },
                            onLongClick = { showContextDropdown = true }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.messageText,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                DropdownMenu(
                    expanded = showContextDropdown,
                    onDismissRequest = { showContextDropdown = false }
                ) {
                    if (isSent) {
                        DropdownMenuItem(
                            text = { Text("Edit Message", color = NearBlack) },
                            onClick = {
                                showContextDropdown = false
                                onEditClick(message)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete Message", color = AlertRed) },
                        onClick = {
                            showContextDropdown = false
                            onDeleteClick(message)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))

            // Message Meta Timestamp + Delivery Ticks
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    color = NearBlack.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
                if (isSent) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delivered",
                        color = TheMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
