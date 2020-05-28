package org.pacman;

public class runMusic implements Runnable {

    private Music music;
    public runMusic(Music music){
        this.music = music;
    }

    @Override
    public void run() {
            while(!music.killed()){ //this needs to constantly run because music.play sets the runnable in a waiting state until it's notified to play.
                music.getThreadReady();
            }
    }

}
