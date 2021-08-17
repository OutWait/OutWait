package edu.kit.outwait.qrCode.generator

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class QRCodeGenerator {
    /**
     * Contains parameters as size of qr-code
     */
    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 800
    }

    /**
     * Generates by passed string a bitmap
     *
     * @param text Value of qr-code
     * @return Value as bitmap
     */
     fun generateQRCode(text: String): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
            for (x in 0 until WIDTH) {
                for (y in 0 until HEIGHT) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
        }
        return bitmap
    }


}
