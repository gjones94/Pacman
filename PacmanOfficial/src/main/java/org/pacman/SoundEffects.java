package org.pacman;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundEffects {

    public SoundEffects(){
    }

    public void play(String name){
        URL file =  getClass().getResource("/" + name);
        try(AudioInputStream as = AudioSystem.getAudioInputStream(file)){

            Clip clip = AudioSystem.getClip();
            clip.open(as);
            clip.start();

        }catch(UnsupportedAudioFileException | IOException | LineUnavailableException e){
            System.out.println(e.getMessage());
        };
    }
}
