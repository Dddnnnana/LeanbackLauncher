package com.google.android.exoplayer2.extractor;

public abstract interface ExtractorOutput
{
  public abstract void endTracks();
  
  public abstract void seekMap(SeekMap paramSeekMap);
  
  public abstract TrackOutput track(int paramInt);
}


/* Location:              ~/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/exoplayer2/extractor/ExtractorOutput.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */