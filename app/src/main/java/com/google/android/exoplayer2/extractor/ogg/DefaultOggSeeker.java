package com.google.android.exoplayer2.extractor.ogg;

import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.util.Assertions;
import java.io.EOFException;
import java.io.IOException;

final class DefaultOggSeeker
  implements OggSeeker
{
  private static final int DEFAULT_OFFSET = 30000;
  public static final int MATCH_BYTE_RANGE = 100000;
  public static final int MATCH_RANGE = 72000;
  private static final int STATE_IDLE = 3;
  private static final int STATE_READ_LAST_PAGE = 1;
  private static final int STATE_SEEK = 2;
  private static final int STATE_SEEK_TO_END = 0;
  private long end;
  private long endGranule;
  private final long endPosition;
  private final OggPageHeader pageHeader = new OggPageHeader();
  private long positionBeforeSeekToEnd;
  private long start;
  private long startGranule;
  private final long startPosition;
  private int state;
  private final StreamReader streamReader;
  private long targetGranule;
  private long totalGranules;
  
  public DefaultOggSeeker(long paramLong1, long paramLong2, StreamReader paramStreamReader, int paramInt, long paramLong3)
  {
    if ((paramLong1 >= 0L) && (paramLong2 > paramLong1)) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkArgument(bool);
      this.streamReader = paramStreamReader;
      this.startPosition = paramLong1;
      this.endPosition = paramLong2;
      if (paramInt != paramLong2 - paramLong1) {
        break;
      }
      this.totalGranules = paramLong3;
      this.state = 3;
      return;
    }
    this.state = 0;
  }
  
  private long getEstimatedPosition(long paramLong1, long paramLong2, long paramLong3)
  {
    paramLong2 = paramLong1 + ((this.endPosition - this.startPosition) * paramLong2 / this.totalGranules - paramLong3);
    paramLong1 = paramLong2;
    if (paramLong2 < this.startPosition) {
      paramLong1 = this.startPosition;
    }
    paramLong2 = paramLong1;
    if (paramLong1 >= this.endPosition) {
      paramLong2 = this.endPosition - 1L;
    }
    return paramLong2;
  }
  
  public OggSeekMap createSeekMap()
  {
    if (this.totalGranules != 0L) {
      return new OggSeekMap(null);
    }
    return null;
  }
  
  public long getNextSeekPosition(long paramLong, ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    if (this.start == this.end) {
      return -(this.startGranule + 2L);
    }
    long l = paramExtractorInput.getPosition();
    if (!skipToNextPage(paramExtractorInput, this.end))
    {
      if (this.start == l) {
        throw new IOException("No ogg page can be found.");
      }
      return this.start;
    }
    this.pageHeader.populate(paramExtractorInput, false);
    paramExtractorInput.resetPeekPosition();
    paramLong -= this.pageHeader.granulePosition;
    int j = this.pageHeader.headerSize + this.pageHeader.bodySize;
    if ((paramLong < 0L) || (paramLong > 72000L))
    {
      if (paramLong < 0L)
      {
        this.end = l;
        this.endGranule = this.pageHeader.granulePosition;
      }
      while (this.end - this.start < 100000L)
      {
        this.end = this.start;
        return this.start;
        this.start = (paramExtractorInput.getPosition() + j);
        this.startGranule = this.pageHeader.granulePosition;
        if (this.end - this.start + j < 100000L)
        {
          paramExtractorInput.skipFully(j);
          return -(this.startGranule + 2L);
        }
      }
      if (paramLong <= 0L) {}
      for (int i = 2;; i = 1)
      {
        l = i * j;
        return Math.min(Math.max(paramExtractorInput.getPosition() - l + (this.end - this.start) * paramLong / (this.endGranule - this.startGranule), this.start), this.end - 1L);
      }
    }
    paramExtractorInput.skipFully(j);
    return -(this.pageHeader.granulePosition + 2L);
  }
  
  public long read(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    switch (this.state)
    {
    default: 
      throw new IllegalStateException();
    case 3: 
      l1 = -1L;
    case 0: 
      long l2;
      do
      {
        return l1;
        this.positionBeforeSeekToEnd = paramExtractorInput.getPosition();
        this.state = 1;
        l2 = this.endPosition - 65307L;
        l1 = l2;
      } while (l2 > this.positionBeforeSeekToEnd);
    case 1: 
      this.totalGranules = readGranuleOfLastPage(paramExtractorInput);
      this.state = 3;
      return this.positionBeforeSeekToEnd;
    }
    if (this.targetGranule == 0L) {}
    for (long l1 = 0L;; l1 = skipToPageOfGranule(paramExtractorInput, this.targetGranule, -(2L + l1)))
    {
      this.state = 3;
      return -(2L + l1);
      l1 = getNextSeekPosition(this.targetGranule, paramExtractorInput);
      if (l1 >= 0L) {
        return l1;
      }
    }
  }
  
  long readGranuleOfLastPage(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    skipToNextPage(paramExtractorInput);
    this.pageHeader.reset();
    while (((this.pageHeader.type & 0x4) != 4) && (paramExtractorInput.getPosition() < this.endPosition))
    {
      this.pageHeader.populate(paramExtractorInput, false);
      paramExtractorInput.skipFully(this.pageHeader.headerSize + this.pageHeader.bodySize);
    }
    return this.pageHeader.granulePosition;
  }
  
  public void resetSeeking()
  {
    this.start = this.startPosition;
    this.end = this.endPosition;
    this.startGranule = 0L;
    this.endGranule = this.totalGranules;
  }
  
  void skipToNextPage(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    if (!skipToNextPage(paramExtractorInput, this.endPosition)) {
      throw new EOFException();
    }
  }
  
  boolean skipToNextPage(ExtractorInput paramExtractorInput, long paramLong)
    throws IOException, InterruptedException
  {
    paramLong = Math.min(3L + paramLong, this.endPosition);
    byte[] arrayOfByte = new byte['ࠀ'];
    int i;
    for (int j = arrayOfByte.length;; j = i)
    {
      i = j;
      if (paramExtractorInput.getPosition() + j > paramLong)
      {
        j = (int)(paramLong - paramExtractorInput.getPosition());
        i = j;
        if (j < 4) {
          return false;
        }
      }
      paramExtractorInput.peekFully(arrayOfByte, 0, i, false);
      j = 0;
      while (j < i - 3)
      {
        if ((arrayOfByte[j] == 79) && (arrayOfByte[(j + 1)] == 103) && (arrayOfByte[(j + 2)] == 103) && (arrayOfByte[(j + 3)] == 83))
        {
          paramExtractorInput.skipFully(j);
          return true;
        }
        j += 1;
      }
      paramExtractorInput.skipFully(i - 3);
    }
  }
  
  long skipToPageOfGranule(ExtractorInput paramExtractorInput, long paramLong1, long paramLong2)
    throws IOException, InterruptedException
  {
    this.pageHeader.populate(paramExtractorInput, false);
    while (this.pageHeader.granulePosition < paramLong1)
    {
      paramExtractorInput.skipFully(this.pageHeader.headerSize + this.pageHeader.bodySize);
      paramLong2 = this.pageHeader.granulePosition;
      this.pageHeader.populate(paramExtractorInput, false);
    }
    paramExtractorInput.resetPeekPosition();
    return paramLong2;
  }
  
  public long startSeek(long paramLong)
  {
    boolean bool;
    if ((this.state == 3) || (this.state == 2))
    {
      bool = true;
      Assertions.checkArgument(bool);
      if (paramLong != 0L) {
        break label54;
      }
    }
    label54:
    for (paramLong = 0L;; paramLong = this.streamReader.convertTimeToGranule(paramLong))
    {
      this.targetGranule = paramLong;
      this.state = 2;
      resetSeeking();
      return this.targetGranule;
      bool = false;
      break;
    }
  }
  
  private class OggSeekMap
    implements SeekMap
  {
    private OggSeekMap() {}
    
    public long getDurationUs()
    {
      return DefaultOggSeeker.this.streamReader.convertGranuleToTime(DefaultOggSeeker.this.totalGranules);
    }
    
    public long getPosition(long paramLong)
    {
      if (paramLong == 0L) {
        return DefaultOggSeeker.this.startPosition;
      }
      paramLong = DefaultOggSeeker.this.streamReader.convertTimeToGranule(paramLong);
      return DefaultOggSeeker.this.getEstimatedPosition(DefaultOggSeeker.this.startPosition, paramLong, 30000L);
    }
    
    public boolean isSeekable()
    {
      return true;
    }
  }
}


/* Location:              ~/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/exoplayer2/extractor/ogg/DefaultOggSeeker.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */