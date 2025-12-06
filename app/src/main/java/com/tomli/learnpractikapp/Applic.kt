package com.tomli.learnpractikapp

import android.app.Application
import com.tomli.learnpractikapp.database.CollectDB

class Applic: Application() {
    val database by lazy{ CollectDB.createDB(this) }
}