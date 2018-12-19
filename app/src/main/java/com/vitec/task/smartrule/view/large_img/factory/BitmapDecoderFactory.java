package com.vitec.task.smartrule.view.large_img.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.IOException;

public interface BitmapDecoderFactory {
    BitmapRegionDecoder made() throws IOException;
    int[] getImageInfo();
}