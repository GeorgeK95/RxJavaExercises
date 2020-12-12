package com.georgek.sgfs.demoObservablesSubjects

import androidx.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit


class SharedViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    private val imagesSubject: BehaviorSubject<MutableList<Photo>> =
        BehaviorSubject.createDefault(mutableListOf())

    private val selectedPhotos = MutableLiveData<List<Photo>>()
    private val thumbnailStatus = MutableLiveData<ThumbnailStatus>()

    init {
        imagesSubject
            .subscribe { selectedPhotos.value = it }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun getThumbnailStatus() = thumbnailStatus

    fun getSelectedPhotos() = selectedPhotos

    fun clearPhotos() {
        imagesSubject.value?.clear()
        imagesSubject.onNext(imagesSubject.value!!)
    }

    fun subscribeSelectedPhotos(fragment: PhotosBottomDialogFragment) {
        val newPhotos = fragment.selectedPhotos.share()

        newPhotos
            .doOnComplete {
                Log.v("SharedViewModel", "Completed selecting photos")
            }
            .takeWhile { imagesSubject.value?.size ?: 0 < 6 }
            .filter { newImage ->
                val bitmap = BitmapFactory.decodeResource(fragment.resources, newImage.drawable)
                bitmap.width > bitmap.height
            }
            .filter { newImage ->
                val photos = imagesSubject.value ?: mutableListOf()
                !(photos.map { it.drawable }.contains(newImage.drawable))
            }
            .debounce(
                250, TimeUnit.MILLISECONDS,
                AndroidSchedulers.mainThread()
            )
            .subscribe { photo ->
                imagesSubject.value?.add(photo)
                imagesSubject.onNext(imagesSubject.value!!)
            }
            .addTo(disposables)

        newPhotos.ignoreElements()
            .subscribe {
                thumbnailStatus.postValue(ThumbnailStatus.READY)
            }
            .addTo(disposables)
    }

    fun saveBitmapFromImageView(imageView: ImageView, context: Context): Single<String> {
        return Single.create { emitter ->
            val tmpImg = "${System.currentTimeMillis()}.png"

            val os: OutputStream?

            val collagesDirectory = File(context.getExternalFilesDir(null), "collages")
            if (!collagesDirectory.exists()) {
                collagesDirectory.mkdirs()
            }

            val file = File(collagesDirectory, tmpImg)

            try {
                os = FileOutputStream(file)
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()
                emitter.onSuccess(tmpImg)
            } catch (e: IOException) {
                Log.e("MainActivity", "Problem saving collage", e)
                emitter.onError(e)
            }
        }

        /*return Observable.create { emitter ->
            val tmpImg = "${System.currentTimeMillis()}.png"

            val os: OutputStream?

            val collagesDirectory = File(context.getExternalFilesDir(null), "collages")
            if (!collagesDirectory.exists()) {
                collagesDirectory.mkdirs()
            }

            val file = File(collagesDirectory, tmpImg)

            try {
                os = FileOutputStream(file)
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()
                emitter.onNext(tmpImg)
                emitter.onComplete()
            } catch (e: IOException) {
                Log.e("MainActivity", "Problem saving collage", e)
                emitter.onError(e)
            }
        }*/
    }
}