/*
 * Copyright 2017 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rubensousa.previewseekbar.exoplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.rubensousa.previewseekbar.PreviewDelegate;
import com.github.rubensousa.previewseekbar.base.PreviewLoader;
import com.github.rubensousa.previewseekbar.base.PreviewView;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;

import java.util.ArrayList;
import java.util.List;

public class PreviewExoTimeBar extends DefaultTimeBar implements PreviewView,
        TimeBar.OnScrubListener {

    private List<OnPreviewChangeListener> listeners;
    private PreviewDelegate delegate;
    private int scrubProgress;
    private int duration;
    private int scrubberColor;
    private int frameLayoutId;

    public PreviewExoTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        listeners = new ArrayList<>();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar, 0, 0);
        final int playedColor = a.getInt(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_played_color,
                DEFAULT_PLAYED_COLOR);
        scrubberColor = a.getInt(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_color,
                getDefaultScrubberColor(playedColor));
        a.recycle();

        a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PreviewExoTimeBar, 0, 0);
        frameLayoutId = a.getResourceId(R.styleable.PreviewExoTimeBar_previewFrameLayout, View.NO_ID);

        delegate = new PreviewDelegate(this, scrubberColor);
        delegate.setEnabled(isEnabled());
        addListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!delegate.isSetup() && getWidth() != 0 && getHeight() != 0) {
            FrameLayout frameLayout = findFrameLayout((ViewGroup) getParent(), frameLayoutId);
            if (frameLayout != null) {
                delegate.attachPreviewFrameLayout(frameLayout);
            }
        }
    }

    @Override
    public void setPreviewColorTint(int color) {
        delegate.setPreviewColorTint(color);
    }

    @Override
    public void setPreviewColorResourceTint(int color) {
        delegate.setPreviewColorResourceTint(color);
    }

    @Override
    public void setPreviewLoader(PreviewLoader previewLoader) {
        delegate.setPreviewLoader(previewLoader);
    }

    @Override
    public void attachPreviewFrameLayout(FrameLayout frameLayout) {
        delegate.attachPreviewFrameLayout(frameLayout);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        delegate.setEnabled(enabled);
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        this.duration = (int) duration;
    }

    @Override
    public void setPosition(long position) {
        super.setPosition(position);
        this.scrubProgress = (int) position;
    }

    @Override
    public boolean isShowingPreview() {
        return delegate.isShowing();
    }

    @Override
    public void showPreview() {
        if (isEnabled()) {
            delegate.show();
        }
    }

    @Override
    public void hidePreview() {
        if (isEnabled()) {
            delegate.hide();
        }
    }

    @Override
    public int getProgress() {
        return scrubProgress;
    }

    @Override
    public int getMax() {
        return duration;
    }

    @Override
    public int getThumbOffset() {
        return getResources().getDimensionPixelOffset(R.dimen.previewseekbar_thumb_offset);
    }

    @Override
    public int getDefaultColor() {
        return scrubberColor;
    }

    @Override
    public void addOnPreviewChangeListener(OnPreviewChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOnPreviewChangeListener(OnPreviewChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        for (OnPreviewChangeListener listener : listeners) {
            scrubProgress = (int) position;
            listener.onStartPreview(this);
        }
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        for (OnPreviewChangeListener listener : listeners) {
            scrubProgress = (int) position;
            listener.onPreview(this, (int) position, true);
        }
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        for (OnPreviewChangeListener listener : listeners) {
            setPosition(position);
            listener.onStopPreview(this);
        }
    }

    @Nullable
    private FrameLayout findFrameLayout(ViewGroup parent, int id) {
        if (id == View.NO_ID || parent == null) {
            return null;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getId() == id && child instanceof FrameLayout) {
                return (FrameLayout) child;
            }
        }
        return null;
    }
}
