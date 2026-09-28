package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GroupLinksApplication
import com.example.ui.theme.BorderGray
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        SimpleTopHeader("Privacy Policy", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            PolicySection(
                title = "1. Information We Collect",
                body = "Group Links collects information provided when submitting links, such as group/channel title, description, category, and public WhatsApp URL. For users who sign in, we process basic authentication credentials using Firebase Authentication."
            )

            PolicySection(
                title = "2. Google Play In-App Purchases",
                body = "All digital promotional transactions are processed securely through Google Play Billing. We do not receive, store, or process credit card numbers or financial passwords."
            )

            PolicySection(
                title = "3. Advertising & Google AdMob",
                body = "We use Google Mobile Ads to display non-intrusive advertisements. AdMob may collect device identifiers and usage telemetry in accordance with your consent preferences gathered via the User Messaging Platform (UMP)."
            )

            PolicySection(
                title = "4. Third-Party Links",
                body = "Group Links connects users to public WhatsApp groups and channels. Once you tap Join or Follow, you interact directly with the WhatsApp platform governed by WhatsApp's privacy policy and terms."
            )

            PolicySection(
                title = "5. Data Security",
                body = "We implement industry-standard database rules to safeguard user submissions and prevent unauthorized access or modification."
            )
        }
    }
}

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        SimpleTopHeader("Terms of Service", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            PolicySection(
                title = "1. Community Guidelines",
                body = "All submitted WhatsApp groups and channels must conform to legal and safe community standards. Hate speech, harassment, illegal trades, copyright infringement, and fraud are strictly forbidden."
            )

            PolicySection(
                title = "2. Zero-Tolerance for 18+ and Spam",
                body = "Adult/sexual content, Ponzi schemes, unauthorized phishing, and spam links violate our terms. Any such link submitted will be immediately rejected and banned from public feeds without refund."
            )

            PolicySection(
                title = "3. Promotion Terms",
                body = "Promoting a listing highlights it in prime visibility areas for the stated campaign duration (e.g. 3 days). Promotion does not guarantee a specific number of joins, as user enrollment depends on voluntary member interest."
            )

            PolicySection(
                title = "4. Moderation & Termination",
                body = "The Group Links administration reserves the right to review, reject, or remove any group or channel report that compromises platform integrity."
            )
        }
    }
}

@Composable
fun ContactSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = GroupLinksApplication.instance.listingRepository
    val settings by repository.appSettings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        SimpleTopHeader("Contact Support", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "We are here to help!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Have questions about your listing, promotion campaigns, or reporting inappropriate links? Reach out to our dedicated support team.",
                fontSize = 14.sp,
                color = SecondaryGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Support Email", fontSize = 12.sp, color = SecondaryGray)
                            Text(settings.supportEmail, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${settings.supportEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Group Links Support Inquiry")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Email")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        SimpleTopHeader("About Group Links", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Group Links",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                text = "Version 1.0.0 (Build 1)",
                fontSize = 13.sp,
                color = SecondaryGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Group Links is a discovery and promotion engine designed to connect people with active, engaging WhatsApp groups and public channels worldwide across news, crypto, entertainment, technology, education, and communities.",
                fontSize = 14.sp,
                color = DarkText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Clean • Fast • Mobile-First",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "© 2026 Group Links. All rights reserved.",
                fontSize = 12.sp,
                color = SecondaryGray
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            fontSize = 13.sp,
            color = SecondaryGray,
            lineHeight = 19.sp
        )
    }
}
