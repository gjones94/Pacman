package org.pacman;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SoundEffects {

    private boolean dead = false;
    private boolean play = false;
    private HashMap<String, Clip> clips = new HashMap<>();
    private Clip clip;
    private String[] soundEffects = {"untouchable", "lose", "win", "scream"};

    public SoundEffects(){
        try {
            clip = AudioSystem.getClip();
        }catch(LineUnavailableException e){
            e.printStackTrace();
        }
        for(String str: soundEffects){
            URL file = getClass().getResource("/" + str + ".wav");
            try (AudioInputStream as = AudioSystem.getAudioInputStream(file)) {
                Clip tempClip = AudioSystem.getClip();
                tempClip.open(as);
                clips.put(str, tempClip);
            } catch (UnsupportedAudioFileException |LineUnavailableException | IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public synchronized void selectSound(String name){
//        if(clip.isActive()){
//            clip.stop();
//        }
        clip = clips.get(name);
        clip.setFramePosition(0);
    }

    public synchronized void getThreadReady(){//sets the thread into the while loop, once the boolean is true, and the thread is notified, it will begin playing.
        while(!play){
            try{
                wait();
            }catch (InterruptedException e){

            }
        }
        play = false;
        clip.start();
        notify();
    }

    public synchronized void play(){
        play = true;
        notifyAll();
    }

    public void kill(){
        dead = true;
    }

    public boolean killed(){
        return dead;
    }

    public synchronized void stop(){
        clip.stop();
    }
}
