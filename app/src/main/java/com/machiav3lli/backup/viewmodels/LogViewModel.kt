/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.machiav3lli.backup.items.LogItem
import com.machiav3lli.backup.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(private val appContext: Application)
    : AndroidViewModel(appContext) {

    var logsList = MediatorLiveData<MutableList<LogItem>>()

    private var _refreshActive = MutableLiveData<Boolean>()
    val refreshActive: LiveData<Boolean>
        get() = _refreshActive

    private val _refreshNow = MutableLiveData<Boolean>()
    val refreshNow: LiveData<Boolean>
        get() = _refreshNow

    init {
        refreshList()
    }

    fun finishRefresh() {
        _refreshActive.value = false
        _refreshNow.value = false
    }

    fun refreshList() {
        viewModelScope.launch {
            _refreshActive.value = true
            logsList.value = recreateAppInfoList()
            _refreshNow.value = true
        }
    }

    private suspend fun recreateAppInfoList(): MutableList<LogItem>? {
        return withContext(Dispatchers.IO) {
            val dataList = LogUtils(appContext).readLogs()
            dataList
        }
    }

    fun deleteLog(log: LogItem) {
        viewModelScope.launch {
            delete(log)
            _refreshNow.value = true
        }
    }

    private suspend fun delete(log: LogItem) {
        withContext(Dispatchers.IO) {
            logsList.value?.remove(log)
            log.delete(appContext)
        }
    }
}