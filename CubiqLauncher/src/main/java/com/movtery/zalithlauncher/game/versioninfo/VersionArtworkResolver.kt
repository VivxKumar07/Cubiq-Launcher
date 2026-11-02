package com.movtery.zalithlauncher.game.versioninfo

import androidx.annotation.DrawableRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version

object VersionArtworkResolver {
    @DrawableRes
    fun getArtworkForVersion(version: Version?): Int {
        if (version == null) return R.drawable.art_pc_bundle
        val rawVersion = version.getVersionInfo()?.minecraftVersion
            ?: version.getVersionName()
        return getArtworkForVersionName(rawVersion, version.getVersionName())
    }

    @DrawableRes
    fun getArtworkForVersionName(mcVersion: String, profileName: String = ""): Int {
        val clean = cleanVersion(mcVersion).lowercase()
        val profileLower = profileName.lowercase()

        // Calendar Versions (2026+)
        if (clean.startsWith("26.3") || clean == "26.3" || clean.contains("wilderness") || profileLower.contains("wilderness")) return R.drawable.art_wilderness_bound
        if (clean.startsWith("26.2") || clean == "26.2") return R.drawable.art_chaos_cubed
        if (clean.startsWith("26.1") || clean == "26.1") return R.drawable.art_tiny_takeover
        if (clean.matches(Regex("""^26\.[4-9].*""")) || clean.matches(Regex("""^2[7-9]\..*"""))) return R.drawable.art_pc_bundle

        // Game Drops (1.21.x branch)
        if (clean.startsWith("1.21.11")) return R.drawable.art_mounts_of_mayhem
        if (clean.startsWith("1.21.9") || clean.startsWith("1.21.10")) return R.drawable.art_copper_age
        if (clean.startsWith("1.21.6")) return R.drawable.art_chase_the_skies
        if (clean.startsWith("1.21.5")) return R.drawable.art_spring_to_life
        if (clean.startsWith("1.21.4")) return R.drawable.art_garden_awakens
        if (clean.startsWith("1.21.2") || clean.startsWith("1.21.3")) return R.drawable.art_bundles_bravery
        if (clean == "1.21" || clean.startsWith("1.21.0") || clean.startsWith("1.21.1")) return R.drawable.art_tricky_trials

        // Major Historical Releases
        if (clean.matches(Regex("""^1\.20(\.[0-6])?.*"""))) return R.drawable.art_trails_tales
        if (clean.matches(Regex("""^1\.19(\.[0-4])?.*"""))) return R.drawable.art_wild_update
        if (clean.matches(Regex("""^1\.18(\.[0-2])?.*"""))) return R.drawable.art_caves_cliffs_part2
        if (clean.matches(Regex("""^1\.17(\.[0-1])?.*"""))) return R.drawable.art_caves_cliffs_part1
        if (clean.matches(Regex("""^1\.16(\.[0-5])?.*"""))) return R.drawable.art_nether_update
        if (clean.matches(Regex("""^1\.15(\.[0-2])?.*"""))) return R.drawable.art_buzzybees
        if (clean.matches(Regex("""^1\.14(\.[0-4])?.*"""))) {
            if (profileLower.contains("cat") || profileLower.contains("panda")) {
                return R.drawable.art_cats_pandas
            }
            return R.drawable.art_village_pillage
        }
        if (clean.matches(Regex("""^1\.13(\.[0-2])?.*"""))) return R.drawable.art_update_aquatic
        if (clean.matches(Regex("""^1\.12(\.[0-2])?.*"""))) return R.drawable.art_world_color
        if (clean.matches(Regex("""^1\.8(\.[0-9])?.*"""))) return R.drawable.art_ocean_monument
        if (clean.matches(Regex("""^1\.[2-3](\.[0-2])?.*"""))) return R.drawable.art_mineshaft
        if (clean.matches(Regex("""^1\.[0-1](\.[0-1])?.*"""))) return R.drawable.art_island

        return R.drawable.art_pc_bundle
    }

    private fun cleanVersion(versionStr: String): String {
        return versionStr
            .replace(Regex("""(?i)^(forge|fabric|neoforge|quilt|optifine)[-_]"""), "")
            .replace(Regex("""(?i)[-_](forge|fabric|neoforge|quilt|optifine).*"""), "")
            .trim()
    }
}

