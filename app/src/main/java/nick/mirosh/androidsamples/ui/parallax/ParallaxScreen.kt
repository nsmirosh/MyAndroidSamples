package nick.mirosh.androidsamples.ui.parallax

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nick.mirosh.androidsamples.R
import nick.mirosh.androidsamples.utils.decodeImageFromInternalStorage
import nick.mirosh.androidsamples.utils.downloadImage


private const val TAG = "ParallaxScreen"

var screenWidthPx = 0
var screenHeightPx = 0
val cardHeightDp = 200

@Composable
fun ParallaxScreen2() {
    val bitmaps = remember {
        mutableStateListOf<Bitmap?>(null)
    }
    val pictures =
        listOf(
            R.raw.lukas_dlutko,
            R.raw.amine_msiouri,
            R.raw.connor_danylenko,
            R.raw.felix,
            R.raw.julia_volk,
            R.raw.matthew_montrone,
            R.raw.sam_willis,
            R.raw.pixabay
        )
    val authorsAndLinks =
        listOf(
            "Lukáš Dlutko" to "https://www.pexels.com/@lukas-dlutko-1278617/",
            "Amine M'siouri" to "https://www.pexels.com/@amine-m-siouri-1025778/",
            "Connor Danylenko" to "https://www.pexels.com/@connor-danylenko-534256/",
            "Felix Mittermeier" to "https://www.pexels.com/@felixmittermeier/",
            "Julia Volk" to "https://www.pexels.com/@julia-volk/",
            "Matthew Montrone" to "https://www.pexels.com/@matthew-montrone-230847/",
            "Sam Willis" to "https://www.pexels.com/@sam-willis-457311/",
            "Pixabay" to "https://www.pexels.com/@pixabay/",
        )
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        initScreenWidthAndHeight(configuration, density)
        bitmaps.removeFirstOrNull()
        pictures.forEach {
            decodeBitmap(resources, it)?.let { decodedBitmap ->
                bitmaps.add(
                    Bitmap.createScaledBitmap(
                        decodedBitmap,
                        screenWidthPx,
                        screenHeightPx,
                        true
                    )
                )
                Log.d(
                    TAG,
                    "ParallaxScreen: updating bitmaps, bitmap size ${bitmaps.size}"
                )
            }
        }
    }
    Log.d(TAG, "ParallaxScreen: bitmaps size ${bitmaps.size}")
    if (bitmaps.size == pictures.size) {
        Log.d(TAG, "ParallaxScreen: showing column")
        ScrollableColumn(bitmaps.toList(), authorsAndLinks)
    }
}

@Composable
fun ScrollableColumn(
    bitmaps: List<Bitmap?>,
    authorAndLinkList: List<Pair<String, String>>
) {
    val columnScrollState = rememberScrollState()
    val cardHeight = with(LocalDensity.current) { cardHeightDp.dp.roundToPx() }
    Log.d(TAG, "ScrollableColumn: cardHeight $cardHeight")

    var prevScrollValue by remember { mutableIntStateOf(0) }
    val columnScrollFromTopInPx =
        columnScrollState.value
    prevScrollValue = columnScrollState.value
    Log.d(
        TAG,
        "ScrollableColumn: yMovement $columnScrollFromTopInPx, prevScrollValue $prevScrollValue"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(columnScrollState),
    ) {
        repeat(bitmaps.size) {
            Spacer(modifier = Modifier.height(16.dp))
            InvertedCard(
                originalBitmap = bitmaps[it]!!,
                cardHeight = cardHeight,
                totalColumnScrollFromTop = columnScrollFromTopInPx,
                authorName = authorAndLinkList[it].first,
                authorLink = authorAndLinkList[it].second,
            )
        }
    }
}

fun decodeBitmap(resources: Resources, pictureId: Int): Bitmap? {
    val opts = BitmapFactory.Options().apply {
        inScaled =
            false  // ensure the bitmap is not scaled based on device density
    }
    val inputStream = resources.openRawResource(pictureId)
    return BitmapFactory.decodeResourceStream(
        resources,
        TypedValue(),
        inputStream,
        null,
        opts
    )
}

@Composable
fun ParallaxScreen() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d(TAG, "ParallaxScreen: downloading image")
        withContext(Dispatchers.IO){
            downloadImage(
                context = context,
                imageUrl = "https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg",
                imageName = "some image"
            )
            decodeImageFromInternalStorage(context, "some image")?.let {
                Log.d(TAG, "ParallaxScreen2: image decoded")
                Log.d(TAG, "ParallaxScreen2: image width ${it.width}")
            }
        }
    }
}


@Composable
fun InvertedCard(
    modifier: Modifier = Modifier,
    originalBitmap: Bitmap,
    cardHeight: Int,
    totalColumnScrollFromTop: Int = 0,
    authorName: String,
    authorLink: String
) {
    Card(
        modifier = Modifier
            .height(cardHeightDp.dp)
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Box {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawIntoCanvas { canvas ->
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val yOffset = calculateYOffset(
                        (totalColumnScrollFromTop * 5) / 10,
                        cardHeight,
                        height
                    )
                    val newBitmap =
                        Bitmap.createBitmap(
                            originalBitmap,
                            0,
                            yOffset,
                            width,
                            cardHeight
                        )
                    canvas.nativeCanvas.drawBitmap(newBitmap, 0f, 0f, null)
                }
            }
            val context = LocalContext.current
            val intent = remember {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(authorLink)
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                onClick = { context.startActivity(intent) },
            ) {
                Text(text = "photo by $authorName")
            }
        }
    }
}

fun calculateYOffset(
    totalColumnScrollFromTop: Int,
    cardHeight: Int,
    pictureHeight: Int
) =
    if (totalColumnScrollFromTop <= 0) {
        pictureHeight - cardHeight
    }
    else if (totalColumnScrollFromTop + cardHeight >= pictureHeight) {
        Log.d(TAG, "Entering second case")
        Log.d(
            TAG,
            "calculateYOffset: totalColumnScrollFromTop $totalColumnScrollFromTop"
        )
        Log.d(TAG, "calculateYOffset: cardHeight $cardHeight")
        Log.d(TAG, "calculateYOffset: pictureHeight $pictureHeight")
        Log.d(
            TAG,
            "totalColumnScrollFromTop + cardHeight >= pictureHeight ${totalColumnScrollFromTop + cardHeight >= pictureHeight}"
        )
        0
    }
    else {
        pictureHeight - cardHeight - (totalColumnScrollFromTop)
    }


fun initScreenWidthAndHeight(configuration: Configuration, density: Float) {
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    screenHeightPx = (screenHeightDp * density).toInt()
    screenWidthPx = (screenWidthDp * density).toInt()
}
@Composable
fun OuterClickCounter() {
    Column {
        var outerClicks by remember { mutableIntStateOf(0) }
        Button(onClick = { outerClicks++ }) {
            Text("Outer click trigger")
        }
        InnerClickCounter(outerClicks)
    }
}

@Composable
fun InnerClickCounter(outerClicks: Int) {
    var innerClicks by mutableIntStateOf(0)
    Column {
        Button(onClick = {
            innerClicks++
        })
        {
            Text("Inner clicks = $innerClicks")
        }
        Text("Outer clicks= $outerClicks")
    }
}
