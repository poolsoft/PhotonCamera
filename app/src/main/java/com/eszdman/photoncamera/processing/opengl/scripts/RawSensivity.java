package com.eszdman.photoncamera.processing.opengl.scripts;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.eszdman.photoncamera.processing.opengl.GLFormat;
import com.eszdman.photoncamera.processing.opengl.GLOneScript;
import com.eszdman.photoncamera.processing.opengl.GLProg;
import com.eszdman.photoncamera.processing.opengl.GLTexture;
import com.eszdman.photoncamera.R;

public class RawSensivity extends GLOneScript {
    public RawSensivity(Point size, Bitmap output) {
        super(size, output, new GLFormat(GLFormat.DataType.UNSIGNED_16), R.raw.rawsensivity, "RawSensivity");
    }
    @Override
    public void StartScript() {
        RawParams rawParams = (RawParams)additionalParams;
        GLProg glProg = glOne.glprogram;
        GLTexture glTexture = new GLTexture(size, new GLFormat(GLFormat.DataType.UNSIGNED_16),rawParams.input);
        glProg.setTexture("RawBuffer",glTexture);
        glProg.setvar("whitelevel",rawParams.oldwhitelevel);
        glProg.setvar("PostRawSensivity",rawParams.sensivity);
        WorkingTexture = new GLTexture(size, new GLFormat(GLFormat.DataType.UNSIGNED_16),null);
        glProg.drawBlocks(WorkingTexture);
        glProg.close();
    }
}
