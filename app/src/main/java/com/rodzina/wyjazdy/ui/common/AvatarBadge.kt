package com.rodzina.wyjazdy.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.ui.theme.personColor

@Composable
fun AvatarBadge(
    user: User,
    memberUserIds: List<String>,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier,
) {
    val color: Color = personColor(user.id, memberUserIds)
    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = user.initials,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = (size.value / 2.4).sp,
        )
    }
}
