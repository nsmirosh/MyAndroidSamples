package nick.mirosh.androidsamples.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@Composable
fun SimpleListScreenContent(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val pokemon by viewModel.pokemon.collectAsStateWithLifecycle(listOf())

    val modifier = Modifier.padding(8.dp)
    val rowModifier = Modifier
        .padding(8.dp, 4.dp, 8.dp, 4.dp)
        .fillMaxWidth()

    val imageModifier = Modifier
        .height(150.dp)
        .padding(8.dp)
        .width(200.dp)
        .clip(shape = RoundedCornerShape(8.dp))

    if (pokemon.isNotEmpty()) {
        LazyColumn {
            items(pokemon.size) { index ->
                val pokemon = pokemon[index]
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = modifier
                ) {
                    Row(
                        modifier = rowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            contentScale = ContentScale.FillBounds,
                            modifier = imageModifier,
                            model = getImageUrl(pokemon.url),
                            contentDescription = "Translated description of what the image contains"
                        )
                        Text(
                            text = pokemon.name,
                            lineHeight = 18.sp,
                            fontSize = 14.sp
                        )
                    }
                }

            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved articles",
                fontSize = 24.sp,
            )
        }
    }

}

fun getImageUrl(pokemonUrl: String): String {

    val regex = Regex("\\d+(?=/[^/]*$)")
    val matches = regex.findAll(pokemonUrl)
    val id = matches.lastOrNull()?.value
    return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}