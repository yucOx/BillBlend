package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.User
import com.R.R.model.UserRepository
import com.google.android.gms.tasks.Task

class AccountViewModel(
) : ViewModel() {
    private val _repository: UserRepository = UserRepository()
    private var _user = _repository.getUser()

    fun signIn(mail: String, password: String): Task<Boolean> {
        return _repository.signIn(mail, password)
    }

    fun createAccount(user: User, password: String, pfp: String): Task<Boolean> {
        return _repository.createAccount(user, password, pfp)
    }

    fun isAnyoneIn(): Boolean {
        return _repository.isAnyoneIn()
    }

    fun cleanAndSetUserInfo(name: String, surname: String, mail: String): User {
        if (name.startsWith(" ")) {
            val _name = name.replace(" ", "")
            _user.name = _name
        } else
            _user.name = name

        if (surname.startsWith(" ") || surname.endsWith("")) {
            val _surname = surname.replace(" ", "")
            _user.surname = _surname
        } else
            _user.surname = surname

        if (mail.startsWith(" ") || mail.endsWith("")) {
            val _mail = mail.replace(" ", "")
            _user.mail = _mail
        } else
            _user.mail = mail

        _user.pfpUri = ""
        return _user
    }

    fun checkBlankArea(user: User): Boolean {
        if (user.name?.isBlank() == true)
            return true
        if (user.surname?.isBlank() == true)
            return true
        return user.mail?.isBlank() == true
    }

    fun getUser(): User {
        return _user
    }
}