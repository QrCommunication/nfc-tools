package com.nfcemulator

import android.app.Application
import com.nfcemulator.di.allModules
import com.topjohnwu.superuser.Shell
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NfcEmulatorApp : Application() {

    init {
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                .setTimeout(30)
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Warm up Shell in background to detect root early
        Shell.getShell { }

        startKoin {
            androidContext(this@NfcEmulatorApp)
            modules(allModules)
        }
    }
}
