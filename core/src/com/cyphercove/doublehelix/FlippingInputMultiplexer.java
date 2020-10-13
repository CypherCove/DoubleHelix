package com.cyphercove.doublehelix;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

public class FlippingInputMultiplexer extends InputMultiplexer {

    public FlippingInputMultiplexer() {
        super();
    }

    public FlippingInputMultiplexer(InputProcessor... processors) {
        super(processors);
    }

    private int applyX (int screenX){
        if (Settings.flipH)
            return Gdx.graphics.getWidth() - screenX;
        return screenX;
    }

    private int applyY (int screenY){
        if (Settings.flipV)
            return Gdx.graphics.getHeight() - screenY;
        return screenY;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return super.touchDown(applyX(screenX), applyY(screenY), pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return super.touchUp(applyX(screenX), applyY(screenY), pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return super.touchDragged(applyX(screenX), applyY(screenY), pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return super.mouseMoved(applyX(screenX), applyY(screenY));
    }
}
