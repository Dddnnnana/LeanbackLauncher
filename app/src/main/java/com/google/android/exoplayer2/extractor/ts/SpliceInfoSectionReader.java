package com.google.android.exoplayer2.extractor.ts;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.TimestampAdjuster;

public final class SpliceInfoSectionReader
  implements SectionPayloadReader
{
  private boolean formatDeclared;
  private TrackOutput output;
  private TimestampAdjuster timestampAdjuster;
  
  public void consume(ParsableByteArray paramParsableByteArray)
  {
    if (!this.formatDeclared)
    {
      if (this.timestampAdjuster.getTimestampOffsetUs() == -9223372036854775807L) {
        return;
      }
      this.output.format(Format.createSampleFormat(null, "application/x-scte35", this.timestampAdjuster.getTimestampOffsetUs()));
      this.formatDeclared = true;
    }
    int i = paramParsableByteArray.bytesLeft();
    this.output.sampleData(paramParsableByteArray, i);
    this.output.sampleMetadata(this.timestampAdjuster.getLastAdjustedTimestampUs(), 1, i, 0, null);
  }
  
  public void init(TimestampAdjuster paramTimestampAdjuster, ExtractorOutput paramExtractorOutput, TsPayloadReader.TrackIdGenerator paramTrackIdGenerator)
  {
    this.timestampAdjuster = paramTimestampAdjuster;
    this.output = paramExtractorOutput.track(paramTrackIdGenerator.getNextId());
    this.output.format(Format.createSampleFormat(null, "application/x-scte35", null, -1, null));
  }
}


/* Location:              ~/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/exoplayer2/extractor/ts/SpliceInfoSectionReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */