package it.italiandudes.bot6329.module;

import it.italiandudes.bot6329.error.ModuleError;
import it.italiandudes.bot6329.exception.ModuleException;
import it.italiandudes.bot6329.exception.module.localization.*;
import it.italiandudes.bot6329.localization.Localization;
import it.italiandudes.bot6329.util.Resource;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Scanner;

@SuppressWarnings("unused")
public final class ModuleLocalization extends BaseModule {

    // Attributes
    private static final String MODULE_NAME = "Localization";
    private static final HashMap<Localization, JSONObject> langMap = new HashMap<>();

    // Fallback LangMap Reader
    private synchronized void initFallbackMap() throws ModuleError {
        if (langMap.containsKey(Localization.FALLBACK)) return;
        Scanner jsonReader = new Scanner(Resource.getAsStream(Localization.getFallbackFilepath()), "UTF-8");
        StringBuilder fallbackJsonBuffer = new StringBuilder();
        while (jsonReader.hasNext()) {
            fallbackJsonBuffer.append(jsonReader.nextLine()).append('\n');
        }
        jsonReader.close();
        try {
            langMap.put(Localization.FALLBACK, new JSONObject(fallbackJsonBuffer.toString()));
        } catch (JSONException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError("Localization Module Load: Failed! (Reason: fallback JSON parsing failed)");
        }
    }

    // Module Management Methods
    public synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log("Localization Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        initFallbackMap();

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log("Localization Module Load: Successful!");
    }
    public synchronized void unloadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log("Localization Module Unload: Started!");
        moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        langMap.clear();

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log("Localization Module Unload: Successful!");
    }
    public synchronized void reloadModule() throws ModuleException, ModuleError {
        Logger.log("Localization Module Reload: Started!");
        moduleReloadPreliminaryCheck(MODULE_NAME);
        setModuleState(ModuleState.RELOADING);

        try {
            unloadModule(true);
            loadModule(true);
        } catch (ModuleException | ModuleError e) {
            langMap.clear();
            throw e;
        }

        setModuleState(ModuleState.LOADED);
        Logger.log("Localization Module Reload: Successful!");
    }

    // Module Methods
    public synchronized void loadLocalizationMap(@Nullable final Localization localization) throws LocalizationModuleException {
        Logger.log("Localization Map \"" + localization + "\" Load: Started!");
        if (getModuleState() != ModuleState.LOADED) throw new LocalizationModuleException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: the localization module is not loaded)");
        if (localization == Localization.FALLBACK) throw new LocalizationMapLoadException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: you can't load again the fallback map)");
        if (langMap.containsKey(localization)) throw new LocalizationMapLoadException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: the map is already loaded)");

        Scanner jsonReader = new Scanner(Resource.getAsStream(Localization.getLangFilepath(localization)), "UTF-8");
        StringBuilder jsonBuffer = new StringBuilder();
        while (jsonReader.hasNext()) {
            jsonBuffer.append(jsonReader.nextLine()).append('\n');
        }
        jsonReader.close();
        try {
            langMap.put(localization, new JSONObject(jsonBuffer.toString()));
        } catch (JSONException e) {
            throw new LocalizationMapLoadException("Localization Map \" " + localization + "\" Load: Failed! (Reason: JSON parsing failed)");
        }

        Logger.log("Localization Map \"" + localization + "\" Load: Successful!");
    }
    public synchronized void unloadLocalizationMap(@NotNull final Localization localization) throws LocalizationModuleException {
        Logger.log("Localization Map\"" + localization + "\" Unload: Started!");
        if (getModuleState() != ModuleState.LOADED) throw new LocalizationModuleException("Localization Map \"" + localization + "\" Unload: Canceled! (Reason: the localization module is not loaded)");
        if (localization == Localization.FALLBACK) throw new LocalizationMapUnloadException("Localization Map \"" + localization + "\" Unload: Canceled! (Reason: you can't unload the fallback map)");
        if (!langMap.containsKey(localization)) throw new LocalizationMapUnloadException("Localization Map \"" + localization + "\" Unload: Canceled! (Reason: this map is not loaded)");
        langMap.remove(localization);
        Logger.log("Localization Map \"" + localization + "\" Unload: Successful!");
    }
    @NotNull
    public String localizeString(@NotNull final Localization localization, @NotNull final String key) throws LocalizationModuleException, LocalizationException {
        if (getModuleState() != ModuleState.LOADED) throw new LocalizationModuleException("Can't use localization: the module is not loaded");
        if (!langMap.containsKey(localization)) throw new LocalizationMapNotLoadedException("The requested localization map is not loaded");
        try {
            return langMap.get(localization).getString(key);
        } catch (JSONException e) {
            try {
                return langMap.get(Localization.FALLBACK).getString(key);
            } catch (JSONException e2) {
                throw new LocalizationException("Unknown key \"" + key + "\"");
            }
        }
    }

    // Instance
    private static ModuleLocalization instance = null;
    @NotNull public static ModuleLocalization getInstance() {
        if (instance == null) instance = new ModuleLocalization();
        return instance;
    }
}
