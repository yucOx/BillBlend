package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.User
import com.R.R.model.UserDataRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.yucox.splitwise.Model.PhotoAndMail

class SearchViewModel : ViewModel() {
    private val _queryList = ArrayList<User>()
    private val _photoAndMails = ArrayList<PhotoAndMail>()
    private val userDataRepository: UserDataRepository = UserDataRepository()
    fun fetchUsers() {
        userDataRepository.fetchAllUsers(
            _queryList
        )
    }

    fun searchUser(nameQuery: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val trimmedQuery = nameQuery.trim().lowercase()

        _queryList.clear()
        _photoAndMails.clear()
        if (trimmedQuery.isNotBlank()) {
            for (a in userDataRepository.getUsersInfo()) {
                val b = a.name?.lowercase()

                if (b != null && b.contains(trimmedQuery)) {
                    _queryList.add(a)
                }
            }
            taskCompletionSource.setResult(true)
        } else
            taskCompletionSource.setResult(false)

        return taskCompletionSource.task
    }

    fun getUserPhoto(): Task<Boolean> {
        return userDataRepository.getUserPhoto(
            _queryList,
            _photoAndMails
        )
    }

    fun getQueryList(): ArrayList<User> {
        return _queryList
    }

    fun getPhotoAndMails(): ArrayList<PhotoAndMail> {
        return _photoAndMails
    }
}