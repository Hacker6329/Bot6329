package it.italiandudes.bot6329.module.localization;

import it.italiandudes.bot6329.module.BaseModule;
import it.italiandudes.bot6329.module.ModuleState;
import it.italiandudes.bot6329.throwable.error.ModuleError;
import it.italiandudes.bot6329.throwable.exception.ModuleException;
import it.italiandudes.bot6329.throwable.exception.module.localization.*;
import it.italiandudes.bot6329.util.JSONManager;
import it.italiandudes.bot6329.util.Resource;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public final class ModuleLocalization extends BaseModule {

    // Attributes
    @NotNull private final HashMap<Localization, JSONObject> langMap = new HashMap<>();

    // Module Management Methods
    @Override
    public synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        initFallbackMap();

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    public synchronized void unloadModule(final boolean isReloading) throws ModuleException, ModuleError {
        unloadModule(isReloading, false);
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        langMap.clear();

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }
    @Override
    public synchronized void reloadModule() throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Reload: Started!");
        moduleReloadPreliminaryCheck(MODULE_NAME);
        setModuleState(ModuleState.RELOADING);

        try {
            unloadModule(true);
            loadModule(true);
        } catch (ModuleException | ModuleError e) {
            unloadModule(false, true);
            throw e;
        }

        setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Reload: Successful!");
    }

    // Module Methods
    private synchronized void initFallbackMap() throws ModuleError {
        if (langMap.containsKey(Localization.FALLBACK)) return;
        try {
            langMap.put(Localization.FALLBACK, JSONManager.readJSON(Resource.getAsStream(Localization.getFallbackFilepath())));
        } catch (JSONException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: fallback JSON parsing failed)", e);
        }
    }
    public synchronized void loadLocalizationMap(@Nullable final Localization localization) throws LocalizationModuleException {
        Logger.log("Localization Map \"" + localization + "\" Load: Started!");
        if (getModuleState() != ModuleState.LOADED) throw new LocalizationModuleException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: the localization module is not loaded)");
        if (localization == Localization.FALLBACK) throw new LocalizationMapLoadException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: you can't load again the fallback map)");
        if (langMap.containsKey(localization)) throw new LocalizationMapLoadException("Localization Map \"" + localization + "\" Load: Canceled! (Reason: the map is already loaded)");

        try {
            langMap.put(localization, JSONManager.readJSON(Resource.getAsStream(Localization.getLangFilepath(localization))));
        } catch (JSONException e) {
            throw new LocalizationMapLoadException("Localization Map \" " + localization + "\" Load: Failed! (Reason: JSON parsing failed)", e);
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
        } catch (JSONException | NullPointerException e) {
            try {
                return langMap.get(Localization.FALLBACK).getString(key);
            } catch (NullPointerException nullPointerException) {
                try {
                    unloadModule(false);
                } catch (ModuleException | ModuleError ignored) {}
                setModuleState(ModuleState.ERROR);
                throw new ModuleError("The localization module is in error", nullPointerException);
            } catch (JSONException e2) {
                throw new LocalizationException("Unknown key \"" + key + "\"", e2);
            }
        }
    }

    // Instance
    private static ModuleLocalization instance = null;
    private ModuleLocalization() {
        super("Localization");
    }
    @NotNull public static ModuleLocalization getInstance() {
        if (instance == null) instance = new ModuleLocalization();
        return instance;
    }
}