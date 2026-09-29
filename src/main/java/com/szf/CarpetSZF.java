package com.szf;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CarpetSZF implements CarpetExtension, ModInitializer {
	public static final String MOD_ID = "szf";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CarpetServer.manageExtension(this);
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	@Override
	public void onGameStarted() {
		// Register SguSettings class with Carpet
		CarpetServer.settingsManager.parseSettingsClass(SZFSettings.class);
	}

	@Override
	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
		WorldEaterHelper.register(dispatcher);
	}

	@Override
	public void onTick(MinecraftServer server) {
		WorldEaterHelper.onServerTick(server);
	}
	@Override
	public Map<String, String> canHasTranslations(String lang) {
		return carpet.utils.Translations.getTranslationFromResourcePath(String.format("assets/carpet-szf/lang/zh_cn.json", lang));
	}

}

