package com.lyrebirdstudio.croppylib.cropview

import android.graphics.Bitmap
import java.net.HttpURLConnection

/**
 * Listener to return an cropped image.
 */
interface OnCropListener {

  /**
   * called when cropping is successful
   *
   * @param bitmap result bitmap
   */
  fun onSuccess()
  fun onSuccess(bitmap: Bitmap ,url : String)

  /**
   * called when cropping is failed
   */
  fun onFailure(e: Exception)
}
