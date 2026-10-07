package com.example.numbermergepuzzle

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numbermergepuzzle.ui.theme.NumberMergePuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NumberMergePuzzleTheme {
                PuzzleGame()
            }
        }
    }
}

@Composable
private fun PuzzleGame() {
    var game by remember {
        mutableStateOf(GameEngine.newGame())
    }

    val context = LocalContext.current

    val preferences = remember(context) {
        context.getSharedPreferences(
            "number_merge_puzzle",
            Context.MODE_PRIVATE
        )
    }

    var bestScore by remember {
        mutableIntStateOf(
            preferences.getInt("best_score", 0)
        )
    }

    LaunchedEffect(game.score) {
        if (game.score > bestScore) {
            bestScore = game.score
            preferences.edit()
                .putInt("best_score", bestScore)
                .apply()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F7EE)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Number Bloom",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF31593A),
                    textAlign = TextAlign.Center
                )

                Button(
                    modifier = Modifier
                        .width(220.dp)
                        .height(64.dp),
                    onClick = {
                        game = GameEngine.newGame()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF527B4D),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "New Game",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    label = "SCORE",
                    value = game.score.toString(),
                    modifier = Modifier.weight(1f)
                )

                ScoreCard(
                    label = "BEST",
                    value = maxOf(bestScore, game.score).toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            GameBoard(
                board = game.board,
                onSwipe = { direction ->
                    game = GameEngine.move(game, direction)
                }
            )

            Text(
                text = "Swipe and merge tiles to grow your number garden",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF52634F),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            when {
                game.isGameOver -> {
                    Text(
                        text = "Game over! Tap New Game to play again.",
                        color = Color(0xFF9B3D36),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                game.hasWon -> {
                    Text(
                        text = "You reached 2048! Keep playing and grow your garden.",
                        color = Color(0xFF31593A),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF527B4D)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameBoard(
    board: List<List<Int>>,
    onSwipe: (MoveDirection) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF93B68D))
            .padding(8.dp)
            .pointerInput(board) {
                var dragX = 0f
                var dragY = 0f

                detectDragGestures(
                    onDragStart = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(dragX) > kotlin.math.abs(dragY)) {
                            if (kotlin.math.abs(dragX) > 30f) {
                                onSwipe(
                                    if (dragX > 0) {
                                        MoveDirection.RIGHT
                                    } else {
                                        MoveDirection.LEFT
                                    }
                                )
                            }
                        } else if (kotlin.math.abs(dragY) > 30f) {
                            onSwipe(
                                if (dragY > 0) {
                                    MoveDirection.DOWN
                                } else {
                                    MoveDirection.UP
                                }
                            )
                        }
                    }
                ) { _, dragAmount ->
                    dragX += dragAmount.x
                    dragY += dragAmount.y
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { row ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { column ->
                        val value = board[row][column]

                        val animatedColor by animateColorAsState(
                            targetValue = tileColor(value),
                            animationSpec = tween(durationMillis = 140),
                            label = "tileColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(animatedColor),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = value,
                                transitionSpec = {
                                    (
                                            fadeIn(tween(120)) +
                                                    scaleIn(
                                                        initialScale = 0.82f,
                                                        animationSpec = tween(120)
                                                    )
                                            ).togetherWith(
                                            fadeOut(tween(80)) +
                                                    scaleOut(
                                                        targetScale = 0.82f,
                                                        animationSpec = tween(80)
                                                    )
                                        )
                                },
                                label = "tileNumber"
                            ) { displayedValue ->
                                if (displayedValue != 0) {
                                    Text(
                                        text = displayedValue.toString(),
                                        color = if (displayedValue <= 64) {
                                            Color(0xFF304B35)
                                        } else {
                                            Color.White
                                        },
                                        fontSize = if (displayedValue >= 1024) {
                                            22.sp
                                        } else {
                                            28.sp
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun tileColor(value: Int): Color {
    return when (value) {
        0 -> Color(0xFFDCE8D5)
        2 -> Color(0xFFF0F5E8)
        4 -> Color(0xFFD9EBC5)
        8 -> Color(0xFFB8D99A)
        16 -> Color(0xFF91C77E)
        32 -> Color(0xFF69B77B)
        64 -> Color(0xFF3F9F76)
        128 -> Color(0xFF287F72)
        256 -> Color(0xFF276B83)
        512 -> Color(0xFF4D679B)
        1024 -> Color(0xFF665D9C)
        else -> Color(0xFF80569A)
    }
}