package com.uzlov.inhunter.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uzlov.inhunter.app.HuntingDataState
import com.uzlov.inhunter.app.exceptions.IllegalAuthStateException
import com.uzlov.inhunter.app.exceptions.PinNotFoundException
import com.uzlov.inhunter.app.exceptions.TeamNotFoundException
import com.uzlov.inhunter.data.net.sources.usecases.PositionsUseCases
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.NullPointerException
import javax.inject.Inject

class HuntingViewModel @Inject constructor(private var positionsUseCases: PositionsUseCases) :
    ViewModel() {

    private val resultLiveData: MutableLiveData<HuntingDataState> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    val observePosition get() : LiveData<HuntingDataState> = resultLiveData

    fun updatePositions(
        email: String,
        lat: Double,
        lng: Double,
    ): LiveData<HuntingDataState> {
        positionsUseCases.updatePositionsWithRetrieveTeam(email, lat, lng)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { compositeDisposable.add(it) }
            .subscribe({
               resultLiveData.postValue(HuntingDataState.Result(it))
            },{
                when(it){
                    is HttpException -> {
                        if (it.code() == 401) resultLiveData.postValue(HuntingDataState.Error(IllegalAuthStateException()))
                        if (it.code() == 404) resultLiveData.postValue(HuntingDataState.Error(TeamNotFoundException()))
                    }
                    is NullPointerException -> {
                        resultLiveData.postValue(HuntingDataState.Error(PinNotFoundException()))
                    }
                }
            })
        return resultLiveData
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        positionsUseCases.clear()
    }
}