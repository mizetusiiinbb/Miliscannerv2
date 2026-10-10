package com.miliconfig.ipv6scanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.ui.theme.StatusOpen
import com.miliconfig.ipv6scanner.ui.theme.StatusRefused
import com.miliconfig.ipv6scanner.ui.theme.StatusTimeout
import com.miliconfig.ipv6scanner.ui.theme.StatusUnknown
import com.miliconfig.ipv6scanner.ui.theme.StatusUnreachable

@Composable
fun StatusBadge(
    status: PortStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        PortStatus.OPEN -> Triple(StatusOpen.copy(alpha = 0.2f), StatusOpen, "OPEN")
        PortStatus.REFUSED -> Triple(StatusRefused.copy(alpha = 0.2f), StatusRefused, "REFUSED")
        PortStatus.TIMEOUT -> Triple(StatusTimeout.copy(alpha = 0.2f), StatusTimeout, "TIMEOUT")
        PortStatus.UNREACHABLE -> Triple(StatusUnreachable.copy(alpha = 0.2f), StatusUnreachable, "UNREACHABLE")
        PortStatus.UNKNOWN -> Triple(StatusUnknown.copy(alpha = 0.2f), StatusUnknown, "UNKNOWN")
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = textColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
