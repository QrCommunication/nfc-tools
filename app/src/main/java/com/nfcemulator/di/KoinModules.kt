package com.nfcemulator.di

import androidx.room.Room
import com.nfcemulator.dump.analyzer.DictionaryManager
import com.nfcemulator.dump.analyzer.KeyCracker
import com.nfcemulator.dump.parser.DumpParserFactory
import com.nfcemulator.nfc.hal.HceEmulatorHal
import com.nfcemulator.nfc.hal.NfcEmulatorHal
import com.nfcemulator.nfc.hal.RootNxpEmulatorHal
import com.nfcemulator.nfc.reader.TagReader
import com.nfcemulator.storage.CryptoManager
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.storage.local.AppDatabase
import com.nfcemulator.ui.emulator.EmulatorViewModel
import com.nfcemulator.ui.home.HomeViewModel
import com.nfcemulator.ui.settings.SettingsViewModel
import com.topjohnwu.superuser.Shell
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "nfc_emulator.db"
        ).build()
    }
    single { get<AppDatabase>().tagDao() }
}

val storageModule = module {
    single { CryptoManager() }
    single { EncryptedFileManager(androidContext(), get()) }
}

val nfcModule = module {
    single { DictionaryManager(androidContext()) }
    single { KeyCracker(get()) }
    single { TagReader() }
    single { DumpParserFactory() }
    single<NfcEmulatorHal> { RootNxpEmulatorHal(androidContext()) }
    single { HceEmulatorHal(androidContext()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { EmulatorViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}

val allModules = listOf(databaseModule, storageModule, nfcModule, viewModelModule)
