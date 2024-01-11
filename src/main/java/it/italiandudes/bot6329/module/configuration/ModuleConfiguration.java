package it.italiandudes.bot6329.module.configuration;

import it.italiandudes.bot6329.module.BaseModule;
import it.italiandudes.bot6329.module.ModuleState;
import it.italiandudes.bot6329.throwable.error.ModuleError;
import it.italiandudes.bot6329.throwable.exception.ModuleException;
import it.italiandudes.bot6329.throwable.exception.module.configuration.ConfigurationModuleException;
import it.italiandudes.bot6329.util.Defs;
import it.italiandudes.bot6329.util.JSONManager;
import it.italiandudes.bot6329.util.Resource;
import it.italiandudes.idl.common.JarHandler;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class ModuleConfiguration extends BaseModule {

    // Attributes
    private JSONObject configuration = null;

    // Module Management Methods
    @Override
    public synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        File configurationFilePath = new File(Defs.JAR_DIRECTORY_PATH + Resource.Configuration.CONFIGURATION_FILENAME);
        if (!configurationFilePath.exists() || !configurationFilePath.isFile()) {
            try {
                JarHandler.copyFileFromJar(new File(Defs.JAR_PATH), Resource.Configuration.CONFIGURATION_FILEPATH, configurationFilePath, true);
            } catch (IOException e) {
                setModuleState(ModuleState.ERROR);
                throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: default configuration file copy failed into jar directory)");
            }
        }

        try {
            configuration = JSONManager.readJSON(configurationFilePath);
        } catch (FileNotFoundException fileNotFoundException) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: configuration file not found, this shouldn't happen)", fileNotFoundException);
        } catch (JSONException jsonException) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the configuration file syntax is invalid)", jsonException);
        }

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

        configuration = null;

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
    @Nullable
    public Object getConfigValue(@NotNull final String key) throws ConfigurationModuleException {
        if (getModuleState() != ModuleState.LOADED) throw new ConfigurationModuleException("The configuration module is not loaded");
        if (configuration.isNull(key)) return null;
        else return configuration.get(key);
    }

    // Instance
    private static ModuleConfiguration instance = null;
    private ModuleConfiguration() {
        super("Configuration");
    }
    @NotNull
    public static ModuleConfiguration getInstance() {
        if (instance == null) instance = new ModuleConfiguration();
        return instance;
    }
}
