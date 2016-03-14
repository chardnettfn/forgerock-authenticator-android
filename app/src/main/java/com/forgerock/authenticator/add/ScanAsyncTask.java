/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator.add;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class responsible for continuously checking the camera output for a QR code and decoding it.
 * This runs as a separate thread.
 */
public class ScanAsyncTask extends AsyncTask<Void, Void, String> implements PreviewCallback {
    private static class Data {
        public byte[] data;
        Camera.Size   size;
    }

    private final BlockingQueue<Data> mBlockingQueue;
    private final Reader              mReader;

    /**
     * Create this AsyncTask, and initialise the QR Code Reader.
     */
    public ScanAsyncTask() {
        mBlockingQueue = new LinkedBlockingQueue<Data>(5);
        mReader = new QRCodeReader();
    }

    @Override
    protected String doInBackground(Void... args) {
        while (true) {
            try {
                Data data = mBlockingQueue.take();
                LuminanceSource ls = new PlanarYUVLuminanceSource(
                        data.data, data.size.width, data.size.height,
                        0, 0, data.size.width, data.size.height, false);
                Result r = mReader.decode(new BinaryBitmap(new HybridBinarizer(ls)));
                return r.getText();
            } catch (InterruptedException e) {
                return null;
            } catch (NotFoundException e) {
            } catch (ChecksumException e) {
            } catch (FormatException e) {
            } catch (ArrayIndexOutOfBoundsException e) {
            } finally {
                mReader.reset();
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Data d = new Data();
        d.data = data;
        d.size = camera.getParameters().getPreviewSize();
        mBlockingQueue.offer(d);
    }
}
