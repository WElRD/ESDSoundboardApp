package vv3ird.ESDSoundboardApp.player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vv3ird.ESDSoundboardApp.AudioApp;

	/**
	 * AudioPlayer for a streamable Clip alternative
	 * @author wkiv894
	 *
	 */
	public class AudioPlayer implements Runnable
	{

		private static Logger logger = LogManager.getLogger(AudioPlayer.class);
		
		public static int LOOP_FOREVER = -1;
		
		private String sound = null;
		
		private String audioFile = null;
		
		private int loop = 0;
		
		private List<LineListener> listeners = new LinkedList<>();
		
		private Object listenerLock = new Object();

		private SourceDataLine line = null;
		
		private Mixer.Info mixer = null;
		
		private Thread thread = null;

		private AudioInputStream stream = null;
		
		private Queue<AudioInputStream> backupStream = new LinkedList<>();

		private LineEvent.Type intendetState = LineEvent.Type.CLOSE;

		private float maxGain = 0;

		private float minGain = 0;
		
		private float gain = 0;
		
		public AudioPlayer(String sound, Mixer.Info mixer)
		{
			this.sound = sound;
			this.audioFile = sound;
			this.mixer = mixer;
			try {
		        FloatControl control = (FloatControl) this.getControl(FloatControl.Type.MASTER_GAIN);
		        if (control != null) {
			            maxGain = control.getMaximum();
			            minGain = control.getMinimum(); // negative values all seem to be zero?
				}
		    } catch (IllegalArgumentException e) {
		        // gain not supported
//		        e.printStackTrace(SoundBoardApp.getErrWriter());
		    	e.printStackTrace();
		    }
		}

		public void addLineListener(LineListener listener)
		{
			synchronized (listenerLock) {
				this.listeners.add(listener);
			}
		}

		public void removeLineListener(LineListener listener)
		{
			synchronized (listenerLock) {
				this.listeners.remove(listener);
			}
		}

		public void open() throws IOException, LineUnavailableException, UnsupportedAudioFileException
		{
			intendetState = LineEvent.Type.OPEN;
			stream = getAudioStream(audioFile); 
			AudioFormat format = stream.getFormat();
			
			initBackupStream();

			try {
				line = (SourceDataLine) AudioSystem.getSourceDataLine(format, mixer);
				line.open(stream.getFormat());
				System.out.println(line.getLineInfo().toString());
			} catch (Exception e) {
				// Backup to get a DataLine suitable for the audio format of the file 
				line = AudioSystem.getSourceDataLine(format);
//				e.printStackTrace(SoundBoardApp.getErrWriter());
				e.printStackTrace();
			}
			// Get min and max gain
			try {
		        FloatControl control = (FloatControl) this.getControl(FloatControl.Type.MASTER_GAIN);
		        if (control != null) {
			            maxGain = control.getMaximum();
			            minGain = control.getMinimum(); // negative values all seem to be zero?
			            control.setValue(this.gain);
			            logger.debug("Master gain (max: " + maxGain + ", min: " + minGain + ")");
				}
		    } catch (IllegalArgumentException e) {
		        // gain not supported
		    	logger.error("Could not read master gain values for SourceDataLine " + this.mixer.getName());
		    	e.printStackTrace();
		    }
			this.fireLineEvent(new LineEvent(line, LineEvent.Type.OPEN, 0));
		}
		
		@SuppressWarnings("resource")
		private AudioInputStream getAudioStream(String file) throws IOException, UnsupportedAudioFileException{
			AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(new File(file))));
			// Getting the format and sanitizing it
			AudioFormat format = stream.getFormat();
			if (sound.endsWith(".mp3")) {
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, // Encoding
																			// to
																			// use
						format.getSampleRate(), // sample rate (same as base
												// format)
						16, // sample size in bits (thx to Javazoom)
						format.getChannels(), // # of Channels
						format.getChannels() * 2, // Frame Size
						format.getSampleRate(), // Frame Rate
						format.isBigEndian() // Big Endian
				);
				stream = AudioSystem.getAudioInputStream(format, stream);
			} else if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
						format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2,
						format.getFrameRate(), true); // big endian
				stream = AudioSystem.getAudioInputStream(format, stream);
			}
			return stream;
		}

		public boolean isOpen()
		{
			return this.line != null && this.line.isOpen();
		}
		
		public boolean isActive() 
		{
			return this.isOpen() && this.thread != null && this.thread.isAlive() && intendetState == LineEvent.Type.START;
		}
		
		public void start() 
		{
			if (!isOpen()) {
				try {
					this.open();
				} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
					logger.error("Error opening audiofile: " + this.audioFile);
					logger.error(e);
					e.printStackTrace();
				}
			}
			if (isOpen() && !isActive()) {
				intendetState = LineEvent.Type.START;
				line.start();
				if(this.thread != null) {
					this.thread.interrupt();
					this.thread = null;
				}
				this.thread = new Thread(this);
				thread.start();
				this.fireLineEvent(new LineEvent(line, LineEvent.Type.START, 0));
			}
		}
		
		public void stop(boolean async)
		{
			if (isActive()) {
				Thread t = new Thread(new Runnable() {
					public void run() {
						FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
						// Fadeout if possible
						if (control != null) {
				        	float curr = control.getValue();
				        	float vol = decibelToLinear(curr);
				            float min = control.getMinimum(); // negative values all seem to be zero?
				            float range = curr - min;
				            int steps = AudioApp.getFadeOutMs();
				            float step = vol/steps;
				            for(int i=0; i<steps; i++) {
				            	vol = vol-step;
				            	float sVal = linearToDecibel(vol);
				            	control.setValue(sVal > min ? sVal : min);
				            	try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
				            }
						}
						intendetState = LineEvent.Type.STOP;
						line.drain();
						line.stop();
						fireLineEvent(new LineEvent(line, LineEvent.Type.STOP, 0));	
						line = null;					
					}
				});
				if(async)
					t.start();
				else
					t.run();
			}
		}
		
		public void close()
		{
			if (intendetState != LineEvent.Type.STOP) {
				this.stop(false);
			}
			if (isOpen()) {
				intendetState = LineEvent.Type.CLOSE;
				this.line.close();
				this.fireLineEvent(new LineEvent(line, LineEvent.Type.CLOSE, 0));
				try {
					if (this.stream != null)
						this.stream.close();
					if (this.backupStream.peek() != null)
						this.backupStream.poll().close();
				} catch (IOException e) {
//					e.printStackTrace(SoundBoardApp.getErrWriter());
					e.printStackTrace();
				}
			}
		}
		
		private static float linearToDecibel(float linear) {
			double dB;

			if (linear != 0)
				dB = 20.0f * Math.log10(linear);
			else
				dB = -144.0f;

			return (float) dB;
		}

		private static float decibelToLinear(float dB) {
			float linear = (float) Math.pow(10.0f, dB / 20.0f);
			return linear;
		}
		
		public void loop(int loops)
		{
			this.loop = loops;
		}
		
		public Control[] getControls()
		{
			if (this.line != null)
				return this.line.getControls();
			else 
				return null;
		}
		
		public String getAudioFile() {
			return audioFile;
		}
		
		public Control getControl(javax.sound.sampled.Control.Type control)
		{
			if (this.line != null)
				return this.line.getControl(control);
			else 
				return null;
		}

		@Override
		public void run() {
			try {
				int numRead = 0;
				byte[] buf = new byte[line.getBufferSize()];
				while (((numRead = stream.read(buf, 0, buf.length)) >= 0 || this.loop != 0) && intendetState == LineEvent.Type.START) {
					if (numRead < 0) {
						if (backupStream.peek() != null)
							stream = backupStream.poll();
						else
							stream = getAudioStream(audioFile);
						numRead = stream.read(buf, 0, buf.length);
						initBackupStream();
						if (this.loop > 0)
							this.loop--;
						if (numRead < 0)
							break;
					}
					int offset = 0;
					while (offset < numRead) {
						offset += line.write(buf, offset, numRead - offset);
					}
				}
				reset();
				stop(true);
			} catch (Exception e) {
				close();
//				e.printStackTrace(SoundBoardApp.getErrWriter());
				e.printStackTrace();
			}
		}
		
		public void reset() throws IOException, UnsupportedAudioFileException
		{
			if (this.stream != null)
				try {
					this.stream.close();
				} catch (IOException e) {
//					e.printStackTrace(SoundBoardApp.getErrWriter());
					e.printStackTrace();
				}
			if (this.backupStream.peek() != null) 
				this.stream = this.backupStream.poll();
			else
				this.stream = getAudioStream(audioFile);
		}

		private void fireLineEvent(LineEvent evnt) {
			synchronized (listenerLock) {
				for (LineListener lineListener : listeners) {
					lineListener.update(evnt);
				}
			}
		}

		public void initBackupStream() {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						backupStream.add(getAudioStream(audioFile));
					} catch (IOException | UnsupportedAudioFileException e) {
//						e.printStackTrace(SoundBoardApp.getErrWriter());
						e.printStackTrace();
					}
				}
			});
			t.start();
		}

		public float getMaxGain()
		{
			return this.maxGain;
		}

		public float getMinGain()
		{
			// TODO Auto-generated method stub
			return this.minGain;
		}

		public void setGain(float gain)
		{
			try {
		        FloatControl control = (FloatControl) this.getControl(FloatControl.Type.MASTER_GAIN);
		        if (control != null) {
		        	gain = gain < minGain ? minGain : gain > maxGain ? maxGain : gain;
		        	this.gain = gain;
		        	control.setValue(this.gain);
				}
		    } catch (IllegalArgumentException e) {
		        // gain not supported
//		        e.printStackTrace(SoundBoardApp.getErrWriter());
				e.printStackTrace();
		    }
		}

		public float getGain()
		{
			try {
		        FloatControl control = (FloatControl) this.getControl(FloatControl.Type.MASTER_GAIN);
		        if (control != null) {
		        	return control.getValue();
				}
		    } catch (IllegalArgumentException e) {
		        // gain not supported
//		        e.printStackTrace(SoundBoardApp.getErrWriter());
				e.printStackTrace();
		    }
			return 0;
		}
		
		
		public void cleanup()
		{
		}
	}