package alexander.skornyakov.catordog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File

class MainViewModel(val context: Context) : ViewModel(){
    val image = MutableLiveData<Bitmap>()
    var score = -1
    private lateinit var _model : Module
    val model : Module
        get() {
            if(!::_model.isInitialized || _model==null){
                _model = Module.load(Helper.assetFilePath(context,"m.pt"))
            }
            return _model
        }

    fun analyze(file: File) {
        var bitmap = BitmapFactory.decodeStream(file.inputStream())
        bitmap = getResizedBitmap(bitmap, 220,220)
        image.postValue(bitmap)
        val input = TensorImageUtils.bitmapToFloat32Tensor(bitmap,0,0,220,220,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        val output = model?.forward(IValue.from(input))?.toTensor()
        val scores = output?.dataAsFloatArray
        score=scores!!.indexOf(scores.min()!!)
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)
        matrix.postRotate(90f)
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
    }
}