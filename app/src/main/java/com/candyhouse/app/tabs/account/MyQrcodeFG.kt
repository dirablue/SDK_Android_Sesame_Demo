/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.candyhouse.app.tabs.account

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.candyhouse.R
import com.candyhouse.app.base.BaseNFG
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.sesame.server.CHAccountManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.fg_myqr.*
import java.io.IOException


class MyQrcodeFG : BaseNFG() {
    companion object {
        var mailStr: String? = null
        var givenName: String? = null
        var familyName: String? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_myqr, container, false)
        return view
    }

    @Throws(WriterException::class, IOException::class)
    private fun generateQRCodeImage(text: String, width: Int, height: Int): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val hints = HashMap<EncodeHintType, Any>()
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints)
        val height = bitMatrix.height
        val width = bitMatrix.width
        val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CHAccountManager.getInvitation() {
            it.onSuccess {
                val image = generateQRCodeImage(it, 350, 350)
                qrcode.post {
                    qrcode.setImageBitmap(image)
                }
            }
        }
        mail.text = mailStr
        given_name.text = givenName
        family_name.text = familyName
        headv?.setImageDrawable(avatatImagGenaroter(givenName))
        head?.setImageDrawable(avatatImagGenaroter(givenName))

        left_icon.setOnClickListener {
            findNavController().navigateUp()
        }

    }
}
