/*
 * Cubiq Launcher
 * 512px High-Resolution Isometric Block Icons Registry
 */

package com.movtery.zalithlauncher.ui.components

import com.movtery.zalithlauncher.R

data class IsometricBlockIcon(
    val id: String,
    val name: String,
    val drawableRes: Int
)

object IsometricBlockIcons {
    val ICONS = listOf(
        IsometricBlockIcon("grass", "Grass Block", R.drawable.img_minecraft),
        IsometricBlockIcon("crafting_table", "Crafting Table", R.drawable.block_crafting_table),
        IsometricBlockIcon("furnace", "Furnace", R.drawable.block_furnace),
        IsometricBlockIcon("diamond_block", "Diamond Block", R.drawable.block_diamond_block),
        IsometricBlockIcon("crafter", "Crafter", R.drawable.block_crafter),
        IsometricBlockIcon("trial_spawner", "Trial Spawner", R.drawable.block_trial_spawner),
        IsometricBlockIcon("vault", "Vault", R.drawable.block_vault),
        IsometricBlockIcon("tnt", "TNT", R.drawable.block_tnt),
        IsometricBlockIcon("obsidian", "Obsidian", R.drawable.block_obsidian),
        IsometricBlockIcon("crying_obsidian", "Crying Obsidian", R.drawable.block_crying_obsidian),
        IsometricBlockIcon("netherite", "Netherite Block", R.drawable.block_netherite),
        IsometricBlockIcon("ancient_debris", "Ancient Debris", R.drawable.block_ancient_debris),
        IsometricBlockIcon("command_block", "Command Block", R.drawable.block_command_block),
        IsometricBlockIcon("beacon", "Beacon", R.drawable.block_beacon),
        IsometricBlockIcon("bookshelf", "Bookshelf", R.drawable.block_bookshelf),
        IsometricBlockIcon("cartography_table", "Cartography Table", R.drawable.block_cartography_table),
        IsometricBlockIcon("smithing_table", "Smithing Table", R.drawable.block_smithing_table),
        IsometricBlockIcon("bedrock", "Bedrock", R.drawable.block_bedrock),
        IsometricBlockIcon("emerald", "Emerald Block", R.drawable.block_emerald),
        IsometricBlockIcon("gold", "Gold Block", R.drawable.block_gold),
        IsometricBlockIcon("iron", "Iron Block", R.drawable.block_iron),
        IsometricBlockIcon("lapis", "Lapis Lazuli Block", R.drawable.block_lapis),
        IsometricBlockIcon("redstone", "Redstone Block", R.drawable.block_redstone),
        IsometricBlockIcon("glowstone", "Glowstone", R.drawable.block_glowstone),
        IsometricBlockIcon("sea_lantern", "Sea Lantern", R.drawable.block_sea_lantern),
        IsometricBlockIcon("shulker_box", "Shulker Box", R.drawable.block_shulker_box),
        IsometricBlockIcon("stone", "Stone", R.drawable.block_stone),
        IsometricBlockIcon("ore_diamond", "Diamond Ore", R.drawable.block_ore_diamond),
        IsometricBlockIcon("oak_planks", "Oak Planks", R.drawable.block_oak_planks),
        IsometricBlockIcon("target", "Target Block", R.drawable.block_target),
        IsometricBlockIcon("barrel", "Barrel", R.drawable.block_barrel),
        IsometricBlockIcon("dirt", "Dirt", R.drawable.block_dirt),
        IsometricBlockIcon("glass", "Glass", R.drawable.block_glass),
        IsometricBlockIcon("melon", "Melon", R.drawable.block_melon),
        IsometricBlockIcon("pumpkin", "Pumpkin", R.drawable.block_pumpkin),
        IsometricBlockIcon("note_block", "Note Block", R.drawable.block_note_block),
        IsometricBlockIcon("packed_ice", "Packed Ice", R.drawable.block_packed_ice),
        IsometricBlockIcon("amethyst", "Amethyst Block", R.drawable.block_amethyst)
    )

    fun getIconById(id: String?): IsometricBlockIcon {
        if (id == null) return ICONS[0]
        return ICONS.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ICONS[0]
    }

    fun getDrawableById(id: String?): Int {
        return getIconById(id).drawableRes
    }
}
