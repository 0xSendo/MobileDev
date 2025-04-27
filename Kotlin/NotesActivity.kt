package com.example.baseconverter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.baseconverter.ui.theme.BaseConverterTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


// Data class for a note
data class Note(val id: String, val title: String, val content: String)

class NotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    NotesScreen(
                        username = intent.getStringExtra("logged_in_user") ?: "User",
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(username: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val notesList = remember { mutableStateListOf<Note>() }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentNote by remember { mutableStateOf<Note?>(null) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    // Load notes from SharedPreferences
    LaunchedEffect(Unit) {
        val loadedNotes = loadNotes(context)
        notesList.clear()
        notesList.addAll(loadedNotes)
    }

    // Save notes whenever the list changes
    LaunchedEffect(notesList.size) {
        saveNotes(context, notesList)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            val scale by animateFloatAsState(
                targetValue = if (showAddDialog) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            )
            FloatingActionButton(
                onClick = {
                    newNoteTitle = ""
                    newNoteContent = ""
                    showAddDialog = true
                },
                containerColor = AccentColor,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .scale(scale)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notes...", color = Color.White.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = AccentColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.8f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        notesList.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.content.contains(searchQuery, ignoreCase = true)
                        }
                    ) { note ->
                        NoteItem(
                            note = note,
                            onEditClick = {
                                currentNote = note
                                newNoteTitle = note.title
                                newNoteContent = note.content
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                currentNote = note
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Add Note Dialog
            if (showAddDialog) {
                NoteDialog(
                    title = "Add Note",
                    noteTitle = newNoteTitle,
                    noteContent = newNoteContent,
                    onTitleChange = { newNoteTitle = it },
                    onContentChange = { newNoteContent = it },
                    onConfirm = {
                        if (newNoteTitle.isNotEmpty() && newNoteContent.isNotEmpty()) {
                            notesList.add(
                                Note(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = newNoteTitle,
                                    content = newNoteContent
                                )
                            )
                        }
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            }

            // Edit Note Dialog
            if (showEditDialog && currentNote != null) {
                NoteDialog(
                    title = "Edit Note",
                    noteTitle = newNoteTitle,
                    noteContent = newNoteContent,
                    onTitleChange = { newNoteTitle = it },
                    onContentChange = { newNoteContent = it },
                    onConfirm = {
                        if (newNoteTitle.isNotEmpty() && newNoteContent.isNotEmpty()) {
                            val index = notesList.indexOf(currentNote)
                            if (index != -1) {
                                notesList[index] = currentNote!!.copy(
                                    title = newNoteTitle,
                                    content = newNoteContent
                                )
                            }
                        }
                        showEditDialog = false
                        currentNote = null
                    },
                    onDismiss = {
                        showEditDialog = false
                        currentNote = null
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog && currentNote != null) {
                Dialog(onDismissRequest = { showDeleteDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FrostedBackground,
                        modifier = Modifier
                            .width(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Delete Note",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Are you sure you want to delete this note?",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    notesList.remove(currentNote)
                                    showDeleteDialog = false
                                    currentNote = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete")
                            }
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    currentNote = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = AccentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedBackground)
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = FrostedBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AccentColor
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AccentColor
                    )
                }
            }
        }
    }
}

@Composable
fun NoteDialog(
    title: String,
    noteTitle: String,
    noteContent: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FrostedBackground,
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = onTitleChange,
                    placeholder = { Text("Title", color = Color.White.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = AccentColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = onContentChange,
                    placeholder = { Text("Content", color = Color.White.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = AccentColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = noteTitle.isNotEmpty() && noteContent.isNotEmpty()
                ) {
                    Text("Save")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = AccentColor)
                }
            }
        }
    }
}

// Persistence functions
private fun saveNotes(context: Context, notes: List<Note>) {
    val sharedPrefs = context.getSharedPreferences("NotesPrefs", Context.MODE_PRIVATE)
    val editor = sharedPrefs.edit()
    val gson = Gson()
    val json = gson.toJson(notes)
    editor.putString("notes", json)
    editor.apply()
}

private fun loadNotes(context: Context): List<Note> {
    val sharedPrefs = context.getSharedPreferences("NotesPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPrefs.getString("notes", null)
    return if (json != null) {
        val type = object : TypeToken<List<Note>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    } else {
        emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    BaseConverterTheme {
        NotesScreen(username = "User", onBackClick = {})
    }
}
