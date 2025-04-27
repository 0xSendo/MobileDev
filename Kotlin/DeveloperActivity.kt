package com.example.baseconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme

class DeveloperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightPink
                ) {
                    DeveloperScreen()
                }
            }
        }
    }
}

@Composable
fun DeveloperScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPink)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Team Icon
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder for the team icon
            contentDescription = "Team Icon",
            modifier = Modifier
                .size(60.dp)
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Fit
        )

        // Meet the Team Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Maroon)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MEET THE TEAM",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Team Members
        TeamMember(
            profileImage = R.drawable.icon_1,
            name = "Cudera, Xianne Jewel S.",
            bio = "A 'fake it till you make it' girly and runs by the motto 'it is what it is'.",
            funFact = "Loves sleeping and has a hidden talent for playing the guitar!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TeamMember(
            profileImage = R.drawable.icon_2,
            name = "Daal, Wyben C.",
            bio = "An experienced frontend developer who thrives on solving complex problems and optimizing performance.",
            funFact = "Has a black belt in martial arts and is a coffee connoisseur."
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Vision Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Maroon)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VISION",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vision Statement
        Text(
            text = "\"Innovating the future, one step at a time\"",
            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
            color = Pink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Our team is dedicated to building innovative and user-friendly solutions that make a difference in people's lives. We believe in continuous learning, collaboration, and striving for excellence in everything we do.",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mission Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Maroon)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MISSION",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mission Statement (Placeholder)
        Text(
            text = "To empower users with tools that simplify their lives and foster creativity through technology.",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun TeamMember(profileImage: Int, name: String, bio: String, funFact: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = profileImage),
            contentDescription = "Profile Picture of $name",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Pink,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fun Fact: $funFact",
                style = MaterialTheme.typography.bodySmall,
                color = DarkGray,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeveloperScreenPreview() {
    BaseConverterTheme {
        DeveloperScreen()
    }
}
