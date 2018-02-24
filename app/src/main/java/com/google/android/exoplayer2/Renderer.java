package com.google.android.exoplayer2;

import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.util.MediaClock;
import java.io.IOException;

public abstract interface Renderer
  extends ExoPlayer.ExoPlayerComponent
{
  public static final int STATE_DISABLED = 0;
  public static final int STATE_ENABLED = 1;
  public static final int STATE_STARTED = 2;
  
  public abstract void disable();
  
  public abstract void enable(RendererConfiguration paramRendererConfiguration, Format[] paramArrayOfFormat, SampleStream paramSampleStream, long paramLong1, boolean paramBoolean, long paramLong2)
    throws ExoPlaybackException;
  
  public abstract RendererCapabilities getCapabilities();
  
  public abstract MediaClock getMediaClock();
  
  public abstract int getState();
  
  public abstract SampleStream getStream();
  
  public abstract int getTrackType();
  
  public abstract boolean hasReadStreamToEnd();
  
  public abstract boolean isCurrentStreamFinal();
  
  public abstract boolean isEnded();
  
  public abstract boolean isReady();
  
  public abstract void maybeThrowStreamError()
    throws IOException;
  
  public abstract void render(long paramLong1, long paramLong2)
    throws ExoPlaybackException;
  
  public abstract void replaceStream(Format[] paramArrayOfFormat, SampleStream paramSampleStream, long paramLong)
    throws ExoPlaybackException;
  
  public abstract void resetPosition(long paramLong)
    throws ExoPlaybackException;
  
  public abstract void setCurrentStreamFinal();
  
  public abstract void setIndex(int paramInt);
  
  public abstract void start()
    throws ExoPlaybackException;
  
  public abstract void stop()
    throws ExoPlaybackException;
}


/* Location:              ~/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/exoplayer2/Renderer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */