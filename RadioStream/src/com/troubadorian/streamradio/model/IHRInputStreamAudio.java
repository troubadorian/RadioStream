package com.troubadorian.streamradio.model;

public class IHRInputStreamAudio extends IHRInputStreamBlocking {
	public static final int             kContentTypeAAC = 1;
	public static final int             kContentTypeMP3 = 2;

	protected static final int          kAACFrameHeaderLookaheadBytes = 6;
	protected static final int          kAACSyncwordBytes = 2;
	protected static final int          kMP3FrameHeaderLookaheadBytes = 3;
	protected static final int          kMP3SyncwordBytes = 2;

	protected int                       mContentType;
	protected double					mDuration;
	protected byte[]                    mFixup;
	protected int                       mHighWaterMark;
	protected int						mLowWaterMark;

	protected int						mCount;
	protected int						mPartial;
	
	// returns duration of audio buffer appended
	public double appendAudioBuffer( byte[] buffer ) {
		mDuration = 0;	
		append( buffer );
		return mDuration;
	}
	
	// this is effectively protected now.  use appendAudioBuffer().
	public synchronized void append( byte[] buffer ) {
		byte[]				filteredBuffer;

		if ( ! mStarted || buffer == null || buffer.length <= 0 ) return;
		
		switch ( mContentType ) {
			case kContentTypeAAC:	filteredBuffer = filterAAC( buffer );		break;
//			case kContentTypeMP3:	filteredBuffer = filterMP3( buffer );		break;

			default:				filteredBuffer = buffer;					break;
		}

		// filteredBuffer might be null if either of the filterX() methods queued data into the fixup buffer
		if ( filteredBuffer == null ) return;

		// setting the high water mark has the effect of rate limiting the download speed.
		if ( mHighWaterMark > 0 && mBytesAvailable >= mHighWaterMark ) {
			try { wait(); } catch ( Exception e ) { }
		}
		
		mBytesAvailable += filteredBuffer.length;
		mData.addElement( filteredBuffer );
		
		notify();
	}

	public synchronized void flush() { super.flush(); mFixup = null; }

	public void setContentType( String contentType ) {
		mContentType = contentType.indexOf( "mpeg" ) == -1 ? kContentTypeAAC : kContentTypeMP3;
	}

	public synchronized void setHighWaterMark( int highWaterMark ) { mHighWaterMark = highWaterMark; }
	public void setLowWaterMark( int lowWaterMark ) { mLowWaterMark = lowWaterMark; }

	// protected methods

	protected double aacSampleFrequency( int frequencyIndex ) {
		switch ( frequencyIndex ) {
			case 0x0:			return 96000.0;
			case 0x1:			return 88200.0;
			case 0x2:			return 64000.0;
			case 0x3:			return 48000.0;
			case 0x4:			return 44100.0;
			case 0x5:			return 32000.0;
			case 0x6:			return 24000.0;
			case 0x7:			return 22050.0;
			case 0x8:			return 16000.0;
			case 0x9:			return 12000.0;
			case 0xa:			return 11025.0;
			case 0xb:			return 8000.0;
			
			default:			return 0;
		}
	}
	
	protected byte[] filterAAC( byte[] buffer ) {
		int                             frequencyIndex, i, length, n, offset, size;
		byte[]                          result, work;
		double							sampleFrequency;

		if ( mFixup != null ) {
			work = new byte[ mFixup.length + buffer.length ];

			System.arraycopy( mFixup, 0, work, 0, mFixup.length );
			System.arraycopy( buffer, 0, work, mFixup.length, buffer.length );

			mFixup = null;
			buffer = work;
		}

		// samplesPerFrame for AAC is 1024

		offset = 0;
		length = buffer.length;
		work = new byte[ buffer.length ];

		for ( i = 0, n = length - kAACFrameHeaderLookaheadBytes; i < n; ++i ) {
			if ( ! isAACSyncword( buffer, i ) ) {
				int x = 1; ++x;
				continue;
			}

			frequencyIndex = ((int) buffer[ i + 2 ]) >> 2 & 0x0f;
			if ( ( sampleFrequency = aacSampleFrequency( frequencyIndex ) ) == 0 ) continue;		// validate sample frequency
			
			// Size from ISO 13818-7:2004 section 6.2.1 Table 8, note absence of emphasis field present in ISO 14496-3 section 1.1.2.1 table 1-6,
			// this caused me some headaches for a while.  Even Apple's code in the ADTS_Workaround.cpp is wrong here.
			size  = ( ((int) buffer[ i + 3 ]) & 0x03 ) << 11;
			size |= ( ((int) buffer[ i + 4 ]) & 0xff ) << 3;
			size |= ( ((int) buffer[ i + 5 ]) >> 5 ) & 0x07;

			if ( i + size <= length - kAACSyncwordBytes ) {
				// here we have enough bytes that we can validate the frame:

				// if a syncword for the next frame isn't where we expect it to be,
				// assume that this frame is invalid and look for a new one.
				if ( ! isAACSyncword( buffer, i + size ) ) {
					int x = 1; ++x;
					continue;
				}

				// here we've verified that this frame is good.  Only append bytes if the
				// size of frames after appending is less than or equal to kAudioBufferSize.
				System.arraycopy( buffer, i, work, offset, size );
				offset += size;

				// duration = total number of frames * samples per frame / sample-frequency
				mDuration += 1024.0 / sampleFrequency;
				
				// process next frame
				i += size - 1;

				++mCount;
				
				continue;
			} else {
				// there is not enough data to determine whether the frame is valid,
				// so stick everything from here forward in the fixup buffer
				mPartial = mCount;
				break;
			}
		}
		
		if ( i == n ) {
			mPartial = mCount;
		}

		if ( offset > 0 ) {
			result = new byte[ offset ];

			System.arraycopy( work, 0, result, 0, offset );
		} else {
			result = null;
		}

		if ( ( n = length - i ) > 0 ) {
			mFixup = new byte[ n ];

			System.arraycopy( buffer, i, mFixup, 0, n );
		}

		return result;
	}

	protected byte[] filterMP3( byte[] buffer ) {
		int                             bitrate, i, layerDescription, length, n, offset, padding, sampleFrequency, size, versionID;
		byte[]                          result, work;

		if ( mFixup != null ) {
			work = new byte[ mFixup.length + buffer.length ];

			System.arraycopy( mFixup, 0, work, 0, mFixup.length );
			System.arraycopy( buffer, 0, work, mFixup.length, buffer.length );

			mFixup = null;
			buffer = work;
		}

		offset = 0;
		length = buffer.length;
		work = new byte[ buffer.length ];

		for ( i = 0, n = length - kMP3FrameHeaderLookaheadBytes; i < n; ++i ) {
			if ( ! isMP3Syncword( buffer, i ) ) continue;

			// samplesPerFrame for MPEG 1 Layer II and III (.mp3) and MPEG 2.25 Layer II is 1152
			// samplesPerFrame for MPEG 2.5 Layer III is 576
			// samplesPerFrame for MPEG 1 Layer I is 384
			// samplesPerFrame for MPEG 2.5 Layer I is 192

			bitrate = ((int) buffer[ i + 2 ]) >> 4 & 0x0f;
			layerDescription = ((int) buffer[ i + 1 ]) >> 1 & 0x03;		// 0x00: reserved, 0x01: Layer III, 0x02: Layer II, 0x03: Layer I
			padding = ((int) buffer[ i + 2 ]) >> 1 & 0x01;
			sampleFrequency = ((int) buffer[ i + 2 ]) >> 2 & 0x03;
			versionID = ((int) buffer[ i + 1 ]) >> 3 & 0x03;			// 0x00: MPEG v2.5, 0x01: reserved, 0x02: MPEG v2, 0x03: MPEG v1

			if ( ( bitrate = mp3Bitrate( versionID, layerDescription, bitrate ) ) == 0 ) continue;
			if ( ( sampleFrequency = mp3SampleFrequency( versionID, sampleFrequency ) ) == 0 ) continue;
			if ( ( size = mp3FrameBytes( layerDescription, bitrate, sampleFrequency, padding ) ) == 0 ) continue;

			if ( i + size <= length - kMP3SyncwordBytes ) {
				// here we have enough bytes that we can validate the frame:

				// if a syncword for the next frame isn't where we expect it to be,
				// assume that this frame is invalid and look for a new one.
				if ( ! isMP3Syncword( buffer, i + size ) ) continue;

				// here we've verified that this frame is good.  Only append bytes if the
				// size of frames after appending is less than or equal to kAudioBufferSize.
				System.arraycopy( buffer, i, work, offset, size );
				offset += size;
				
				mDuration += 1152.0 / (double) sampleFrequency;

				// process next frame
				i += size - 1;

				continue;
			} else {
				// there is not enough data to determine whether the frame is valid,
				// so stick everything from here forward in the fixup buffer
				break;
			}
		}

		if ( offset > 0 ) {
			result = new byte[ offset ];

			System.arraycopy( work, 0, result, 0, offset );
		} else {
			result = null;
		}

		if ( ( n = length - i ) > 0 ) {
			mFixup = new byte[ n ];

			System.arraycopy( buffer, i, mFixup, 0, n );
		}
		
		return result;
	}

	protected synchronized int getNextBuffer() {
		mCurrentBufferOffset = 0;

		for ( ;; ) {
			if ( ! mStarted ) return -1;

			if ( mData.size() > 0 ) {
				mCurrentBuffer = (byte[]) mData.elementAt( 0 );
				mData.removeElementAt( 0 );
				
				// if mHighWaterMark is 0 this code does nothing.  Otherwise, if removing
				// mCurrentBuffer from mData has caused the total size of mData to be less
				// than mHighWaterMark wakeup append() (unthrottle the network).
				if ( mBytesAvailable - mCurrentBuffer.length < mHighWaterMark ) notify();
				
				break;
			}

			mCurrentBuffer = null;

			// here we have no buffers available, so allow append() to succeed if it is throttled.
			if ( mHighWaterMark > 0 ) notify();

			// block for data
			try { wait(); } catch ( Exception e ) { }
		};

		return mCurrentBuffer.length;
	}

	protected boolean isAACSyncword( byte[] buffer, int offset ) {
		return ((int) buffer[ offset ] & 0xff) == 0xff && ((int) buffer[ offset + 1 ] & 0xf6) == 0xf0;
	}

	protected boolean isMP3Syncword( byte[] buffer, int offset ) {
		return ((int) buffer[ offset ] & 0xff) == 0xff && ((int) buffer[ offset + 1 ] & 0xe0) == 0xe0;
	}

	protected int mp3Bitrate( int versionID, int layerDescription, int bitrateField ) {
		int                             bitrate;

		final int[]                     v1l2Bitrate = { 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384 };
		final int[]                     v1l3Bitrate = { 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320 };
		final int[]                     v2l1Bitrate = { 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256 };
		final int[]                     v2l2Bitrate = { 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 };

		if ( ( bitrate = bitrateField ) == 0 || bitrateField == 0x0f ) return 0;

		switch ( versionID ) {
			case 1:                                                         return 0;

			case 0:
			case 2: {
				switch ( layerDescription ) {
					case 0:                                                 return 0;
					case 1:
					case 2:     bitrate = v2l2Bitrate[ bitrate - 1 ];       break;
					case 3:     bitrate = v2l1Bitrate[ bitrate - 1 ];       break;
				}
			} break;

			case 3: {
				switch ( layerDescription ) {
					case 0:                                                 return 0;
					case 1:     bitrate = v1l3Bitrate[ bitrate - 1 ];       break;
					case 2:     bitrate = v1l2Bitrate[ bitrate - 1 ];       break;
					case 3:     bitrate <<= 5;                              break;
				}
			} break;
		}

		return bitrate * 1000;
	}

	protected int mp3FrameBytes( int layerDescription, int bitrate, int sampleFrequency, int padding ) {
		switch ( layerDescription ) {
			case 1:
			case 2:     return 144 * bitrate / sampleFrequency + padding;
			case 3:     return ( 12 * bitrate / sampleFrequency + padding ) * 4;
		}

		return 0;
	}

	protected int mp3SampleFrequency( int versionID, int frequencyIndex ) {
		final int[]                     v1SampleFrequency = { 44100, 48000, 32000 };
		final int[]                     v2SampleFrequency = { 22050, 24000, 16000 };
		final int[]                     v25SampleFrequency = { 11025, 12000, 8000 };

		if ( frequencyIndex == 3 ) return 0;

		switch ( versionID ) {
			case 0:                     return v25SampleFrequency[ frequencyIndex ];
			case 2:                     return v2SampleFrequency[ frequencyIndex ];
			case 3:                     return v1SampleFrequency[ frequencyIndex ];
		}

		return 0;
	}
}
