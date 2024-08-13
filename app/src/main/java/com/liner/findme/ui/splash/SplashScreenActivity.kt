package com.liner.findme.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.liner.findme.R
import com.liner.findme.databinding.ActivityAuthenticationBinding
import com.liner.findme.ui.AuthenticationActivity
import com.liner.findme.ui.MainActivity
import com.liner.findme.ui.authentication.AuthenticationViewModel
import com.liner.findme.ui.authentication.AuthenticationViewModelImpl
import com.liner.findme.ui.composable.FindMeLottieAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    // region private properties

    private val viewModel: AuthenticationViewModel by viewModels<AuthenticationViewModelImpl>()

    // endregion

    // region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashLogo()
        }

        lifecycleScope.launch {
            viewModel.isLogged.flowWithLifecycle(lifecycle).onEach { delay(3000) }
                .collectLatest { isLogged ->
                    startActivity(
                        Intent(
                            this@SplashScreenActivity,
                            if (isLogged) MainActivity::class.java else AuthenticationActivity::class.java
                        )
                    )
                    finish()
                    if (!isLogged) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
        }

    }

    // endregion

    // region composable

    @Composable
    fun SplashLogo(){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CanvasNameLogo()

                Text(
                    text = "FindMe",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CanvasCreatorLogo()

                Text(
                    text = "from",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                )
                Text(
                    text = "Liner",
                    fontSize = 28.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                )
            }

        }
    }

    @OptIn(ExperimentalTextApi::class)
    @Preview
    @Composable
    fun CanvasCreatorLogo(){

        val animationDuration = 4500
        val interpolation = remember { Animatable(0f) }

        LaunchedEffect(interpolation) {
            launch {
                interpolation.animateTo(targetValue = 1f, animationSpec = tween(animationDuration, easing = EaseOutElastic))
            }
        }

        Canvas(modifier = Modifier
            .width(200.dp)
            .height(120.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val halfLogoSizeWidth = canvasWidth*0.2f
            val halfLogoSizeHeight = canvasWidth*0.4f

            val imgLogoHalf = Path().apply {
                lineTo(halfLogoSizeWidth, 0f)
                lineTo(halfLogoSizeWidth*0.5f, -halfLogoSizeHeight*0.15f)
                lineTo(halfLogoSizeWidth, -halfLogoSizeHeight)
                close()
            }

            // drawRect(color = Color.Green, size = size)

            rotate((1f-interpolation.value)*(30f) + (interpolation.value)*(0f)) {
                translate(
                    (1f-interpolation.value)*(canvasWidth*0.9f) + (interpolation.value)*(canvasWidth/2 - halfLogoSizeWidth),
                    (1f-interpolation.value)*(canvasHeight*0.4f) + (interpolation.value)*(canvasHeight/2 + halfLogoSizeWidth*0.4f)
                ) {
                    drawPath(path = imgLogoHalf, Color.Black)
                }
            }

            rotate((1f-interpolation.value)*(30f + 180f) + (interpolation.value)*(180f)) {
                translate(
                    (1f-interpolation.value)*(canvasWidth*0.9f) + (interpolation.value)*(canvasWidth/2 - halfLogoSizeWidth),
                    (1f-interpolation.value)*(canvasHeight*0.4f) + (interpolation.value)*(canvasHeight/2 + halfLogoSizeWidth*0.4f)
                ) {
                    drawPath(path = imgLogoHalf, Color.Black)
                }
            }


        }

    }

    @OptIn(ExperimentalTextApi::class)
    @Preview
    @Composable
    fun CanvasNameLogo(){

        val animationDuration = 3000
        val interpolation = remember { Animatable(0f) }

        LaunchedEffect(interpolation) {
            launch {
                interpolation.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(animationDuration, easing = EaseOutBounce),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }

        Canvas(modifier = Modifier
            .width(240.dp)
            .height(130.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val halfLogoSizeWidth = canvasWidth*0.25f
            val halfLogoSizeHeight = canvasWidth*0.4f

            val imgLocation = Path().apply {
                arcTo(
                    rect = Rect(-halfLogoSizeWidth*0.5f, 0f-halfLogoSizeWidth*0.5f, halfLogoSizeWidth*0.5f, halfLogoSizeHeight*0.6f-halfLogoSizeWidth*0.5f),
                    startAngleDegrees = (180f-28f), sweepAngleDegrees = (180f+2*28f), forceMoveTo = false
                )
                lineTo(0f, halfLogoSizeHeight*1f-halfLogoSizeWidth*0.5f)
                close()
            }

            drawOval(
                color = Color.Black,
                topLeft = Offset(canvasWidth/2-(interpolation.value)*halfLogoSizeWidth*0.3f, canvasHeight/2+halfLogoSizeHeight*0.73f-halfLogoSizeWidth*0.2f),
                size = Size((interpolation.value)*2*halfLogoSizeWidth*0.3f, (interpolation.value)*2*halfLogoSizeWidth*0.14f),
                alpha = 0.2f
            )

            translate(
                (canvasWidth/2),
                (1f-interpolation.value)*(canvasHeight/2 - halfLogoSizeWidth*0.7f) + (interpolation.value)*(canvasHeight/2)
            ) {

                drawPath(path = imgLocation, color = Color(206, 16, 16, 255))
                drawCircle(color = Color.White, radius = halfLogoSizeWidth*0.2f, center = Offset(0f, 0f))
            }



        }

    }

    // endregion

}