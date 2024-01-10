package it.italiandudes.bot6329.module;

import it.italiandudes.bot6329.error.ModuleError;
import it.italiandudes.bot6329.exception.module.generic.*;
import org.jetbrains.annotations.NotNull;

public abstract class BaseModule {

    // Attributes
    @NotNull private ModuleState moduleState = ModuleState.NOT_LOADED;

    // Implemented Methods
    protected synchronized void setModuleState(@NotNull final ModuleState moduleState) {
        this.moduleState = moduleState;
    }
    @NotNull public ModuleState getModuleState() {
        return moduleState;
    }
    protected synchronized void moduleLoadPreliminaryCheck(@NotNull final String moduleName, final boolean isReloading) throws ModuleLoadingException, ModuleAlreadyLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Load: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing the module unload)");
            case RELOADING:
                if (!isReloading) throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing the module reload)");
            case LOADED:
                throw new ModuleAlreadyLoadedException(moduleName + "Module Load: Canceled! (Reason: this module is already loaded)");
        }
    }
    protected synchronized void moduleUnloadPreliminaryCheck(@NotNull final String moduleName, final boolean isReloading) throws ModuleUnloadingException, ModuleNotLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Unload: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing the module unload)");
            case NOT_LOADED:
                throw new ModuleNotLoadedException(moduleName + " Module Unload: Canceled! (Reason: the module is not loaded)");
            case RELOADING:
                if (!isReloading) throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing the module reload)");
        }
    }
    protected synchronized void moduleReloadPreliminaryCheck(@NotNull final String moduleName) throws ModuleReloadingException, ModuleNotLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Reload: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing the module unload)");
            case RELOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing the module reload)");
            case NOT_LOADED:
                throw new ModuleNotLoadedException(moduleName + " Module Reload: Canceled! (Reason: the module is not loaded)");
        }
    }
}
