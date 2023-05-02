package com.bennohan.kelasku.helper

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bennohan.kelasku.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ViewBindingHelper {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["imageUrl"], requireAll = false)
        fun loadImageRecipe(view: ImageView, imageUrl: String?) {

            view.setImageDrawable(null)

            if (imageUrl.isNullOrEmpty()) {
                Glide
                    .with(view.context)
                    .load(R.drawable.ic_baseline_person_24)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .apply(RequestOptions.centerCropTransform())
                    .into(view)


            } else {
                imageUrl.let {
                    Glide
                        .with(view.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(view)

                }

            }

        }
        @JvmStatic
        @BindingAdapter(value = ["imageUrlCircle"], requireAll = false)
        fun loadImageRecipeCircle(view: ImageView, imageUrl: String?) {

            view.setImageDrawable(null)

//            imageUrl?.let {
//                Glide
//                    .with(view.context)
//                    .load(imageUrl)
//                    .apply(RequestOptions.circleCropTransform())
//                    .placeholder(R.drawable.ic_baseline_person_24)
//                    .into(view)
//
//
//            }
            if (imageUrl.isNullOrEmpty()) {
                Glide
                    .with(view.context)
                    .load(imageUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(view)


            } else {
                imageUrl.let {
                    Glide
                        .with(view.context)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(view)

                }

            }


        }

    }
}