package com.georgek.sgfs.demoObservablesSubjects

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = getString(R.string.collage)

        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        addButton.setOnClickListener {
            actionAdd()
        }

        clearButton.setOnClickListener {
            actionClear()
        }

        saveButton.setOnClickListener {
            actionSave()
        }

        viewModel.getSelectedPhotos().observe(this, Observer { photos ->
            if (photos.isEmpty()) {
                collageImage.setImageResource(android.R.color.transparent)
                return@Observer
            }

            val bitmaps = photos.map { BitmapFactory.decodeResource(resources, it.drawable) }
            val newBitmap = combineImages(bitmaps)
            collageImage.setImageDrawable(BitmapDrawable(resources, newBitmap))

            updateUi(photos)
        })

        viewModel.getThumbnailStatus().observe(this, Observer {
            if (it == ThumbnailStatus.READY) {
                thumbnail.setImageDrawable(collageImage.drawable)
            }
        })
    }

    private fun updateUi(photos: List<Photo>) {
        saveButton.isEnabled = photos.isNotEmpty() && (photos.size % 2 == 0)
        clearButton.isEnabled = photos.isNotEmpty()
        addButton.isEnabled = photos.size < 6
        title = if (photos.isNotEmpty()) {
            resources.getQuantityString(
                R.plurals.photos_format,
                photos.size, photos.size
            )
        } else {
            getString(R.string.collage)
        }
    }

    private fun actionAdd() {
        val addPhotoBottomDialogFragment = PhotosBottomDialogFragment.newInstance()
        addPhotoBottomDialogFragment.show(supportFragmentManager, "PhotosBottomDialogFragment")
        viewModel.subscribeSelectedPhotos(addPhotoBottomDialogFragment)
    }

    private fun actionClear() {
        viewModel.clearPhotos()
    }

    private fun actionSave() {
        viewModel.saveBitmapFromImageView(collageImage, this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { file ->
                    Toast.makeText(
                        this, "$file saved",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { e ->
                    Toast.makeText(
                        this,
                        "Error saving file :${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
    }
}
