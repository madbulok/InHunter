package com.uzlov.inhunter.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TestMain {

    public static void main(String[] args) {
        String TAG = "test";
        List<Integer> range = new ArrayList<Integer>();
        range.add(2);
        range.add(21);
        Observable.fromIterable(range)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(16, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    System.out.println(v);
                }, e -> {

                });
    }
}
