package ca.gbc.comp3074.scavengerhunt

import android.app.Application
import ca.gbc.comp3074.scavengerhunt.data.AppDatabase
import ca.gbc.comp3074.scavengerhunt.data.Hunt
import ca.gbc.comp3074.scavengerhunt.data.HuntRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * App-wide stuff lives here:
 *   - DB singleton + repository (every Activity grabs them via `repo` extension)
 *   - First-run seeding so the list isn't empty on a fresh install
 *
 * Wired up in AndroidManifest with android:name=".ScavengerHuntApp".
 */
class ScavengerHuntApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { HuntRepository(database.huntDao(), database.userDao()) }

    // SupervisorJob so a single failing coroutine doesn't take down the rest.
    // IO dispatcher because everything we do here is database I/O.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            // only seed once — the count check is the cheap way to detect first run
            if (repository.count() == 0) {
                STARTERS.forEach { repository.insert(it) }
            }
        }
    }

    private companion object {
        // Toronto-flavoured starter hunts. Users will probably nuke these
        // and add their own — that's fine, that's the point.
        val STARTERS = listOf(
            Hunt(
                name = "CN Tower",
                tag = "landmark",
                address = "290 Bremner Blvd",
                city = "Toronto",
                description = "Find the glass floor and look straight down. " +
                        "Hint: 113 storeys up — definitely not for the faint of heart.",
                rating = 4.5f
            ),
            Hunt(
                name = "Kensington Market",
                tag = "neighbourhood",
                address = "Kensington Ave",
                city = "Toronto",
                description = "Snap a photo of the most outrageous mural you can find. " +
                        "Bonus points if you grab a coffee from one of the indie spots.",
                rating = 4.0f
            ),
            Hunt(
                name = "Distillery District",
                tag = "art",
                address = "55 Mill St",
                city = "Toronto",
                description = "Track down the giant LOVE sculpture. Best at night " +
                        "when the cobblestone streets are lit up.",
                rating = 4.5f
            ),
            Hunt(
                name = "St. Lawrence Market",
                tag = "food",
                address = "93 Front St E",
                city = "Toronto",
                description = "Find the peameal bacon sandwich stall. Locals will fight " +
                        "you if you say it's not the best in the city.",
                rating = 5.0f
            ),
            Hunt(
                name = "Casa Loma",
                tag = "landmark",
                address = "1 Austin Terrace",
                city = "Toronto",
                description = "A literal castle in the middle of the city. Find the " +
                        "secret tunnel that connects the main house to the stables.",
                rating = 4.5f
            )
        )
    }
}
