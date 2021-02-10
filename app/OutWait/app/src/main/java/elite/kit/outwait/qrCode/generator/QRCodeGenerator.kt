package elite.kit.outwait.qrCode.generator

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class QRCodeGenerator {
    companion object {
        private const val WITDH = 400
        private const val HEIGHT = 400
    }

     fun generateQRCode(text: String): Bitmap {
        val bitmap = Bitmap.createBitmap(WITDH, HEIGHT, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, WITDH, HEIGHT)
            for (x in 0 until WITDH) {
                for (y in 0 until HEIGHT) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
        }
        return bitmap
    }


}
