package com.zj.play.main.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zj.core.Play
import com.zj.core.util.showToast
import com.zj.model.model.BaseModel
import com.zj.model.model.Login
import com.zj.play.R
import com.zj.play.article.ArticleBroadCast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 版权：Zhujiang 个人版权
 * @author zhujiang
 * 版本：1.5
 * 创建日期：2020/5/17
 * 描述：PlayAndroid
 *
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val accountRepository: AccountRepository
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<LoginState>()
    val state: LiveData<LoginState>
        get() = _state

    fun toLoginOrRegister(account: Account) {
        _state.postValue(Logging)
         if (account.isLogin) {
            login(account)
        } else {
            register(account)
        }
        
        
//         viewModelScope.launch(Dispatchers.IO) {
//             val loginModel: BaseModel<Login> = if (account.isLogin) {
//                 accountRepository.getLogin(account.username, account.password)
//             } else {
//                 accountRepository.getRegister(
//                     account.username,
//                     account.password,
//                     account.password
//                 )
//             }

//             if (loginModel.errorCode == 0) {
//                 val login = loginModel.data
//                 _state.postValue(LoginSuccess(login))
//                 Play.setLogin(true)
//                 Play.setUserInfo(login.nickname, login.username)
//                 withContext(Dispatchers.Main) {
//                     getApplication<Application>().showToast(
//                         if (account.isLogin) getApplication<Application>().getString(R.string.login_success) else getApplication<Application>().getString(
//                             R.string.register_success
//                         )
//                     )
//                 }
//                 ArticleBroadCast.sendArticleChangesReceiver(context = getApplication())
//             } else {
//                 withContext(Dispatchers.Main) {
//                     getApplication<Application>().showToast(loginModel.errorMsg)
//                 }
//                 _state.postValue(LoginError)
//             }
//         }
    }
    
     private fun login(account: Account) {
        viewModelScope.http(
            request = { accountRepository.getLogin(account.username, account.password) },
            response = { success(it, account.isLogin) },
            error = { _state.postValue(LoginError) }
        )
    }


    private fun register(account: Account) {
        viewModelScope.http(
            request = {
                accountRepository.getRegister(
                    account.username,
                    account.password,
                    account.password
                )
            },
            response = { success(it, account.isLogin) },
            error = { _state.postValue(LoginError) }
        )
    }

    private fun success(it: Login?, isLogin: Boolean) {
        it ?: return
        _state.postValue(LoginSuccess(it))
        Play.setLogin(true)
        Play.setUserInfo(it.nickname, it.username)
        ArticleBroadCast.sendArticleChangesReceiver(context = getApplication())
        getApplication<Application>().showToast(
            if (isLogin) getApplication<Application>().getString(R.string.login_success) else getApplication<Application>().getString(
                R.string.register_success
            )
        )
    }

}

data class Account(val username: String, val password: String, val isLogin: Boolean)
sealed class LoginState
object Logging : LoginState()
data class LoginSuccess(val login: Login) : LoginState()
object LoginError : LoginState()
