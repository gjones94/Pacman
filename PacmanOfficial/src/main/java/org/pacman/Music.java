package org.pacman;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Music{
    private boolean dead = false;
    private boolean play = false;
    private HashMap<String, Clip> clips = new HashMap<>();
    private Clip clip;
//    private String[] songs = {"gameMusic","intro", "win", "office", "michael"};
    private String[] songs = {"gameMusic","intense","intro", "lose", "win"};

    public Music(){
        try {
            clip = AudioSystem.getClip();
        }catch(LineUnavailableException e){
            e.printStackTrace();
        }
        for(String str: songs){
            URL file = getClass().getResource("/" + str + ".wav");
            try (AudioInputStream as = AudioSystem.getAudioInputStream(file)) {
                Clip tempClip = AudioSystem.getClip();
                tempClip.open(as);
                clips.put(str, tempClip);
//                tempClip.close();
            } catch (UnsupportedAudioFileException |LineUnavailableException | IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public synchronized void selectSong(String name, boolean resetPosition){
        if(clip.isActive()){
            clip.stop();
        }
        clip = clips.get(name);
        if(resetPosition){
            clip.setFramePosition(0);
        }
    }

    public synchronized void getThreadReady(){//sets the thread into the while loop, once the boolean is true, and the thread is notified, it will begin playing.
        while(!play){
            try{
                wait();
            }catch (InterruptedException e){

            }
        }
        play = false;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
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

    public boolean isPlaying(){
        return clip.isActive();
    }

    public synchronized void stop(){
        clip.stop();
    }
}
