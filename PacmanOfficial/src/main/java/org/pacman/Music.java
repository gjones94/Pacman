package org.pacman;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Music{

    private Thread musicThread;

    private final String musicName;

    private Clip clip;

    private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                URL file = getClass().getResource("/" + musicName);
                try (AudioInputStream as = AudioSystem.getAudioInputStream(file)) {

                    clip = AudioSystem.getClip();
                    clip.open(as);
                    clip.loop(Clip.LOOP_CONTINUOUSLY);

                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                    clip.start();
                }
            }
    };


    public Music(String name){
        this.musicName = name;
    }

    public void play(){
       musicThread = new Thread(runnable);
       musicThread.start();
    }

    public void stop(){
        clip.stop();
        musicThread.interrupt();
    }
}
