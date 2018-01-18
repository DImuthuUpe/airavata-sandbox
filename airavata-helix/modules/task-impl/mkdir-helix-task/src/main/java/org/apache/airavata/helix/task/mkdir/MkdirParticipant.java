package org.apache.airavata.helix.task.mkdir;

import org.apache.airavata.helix.task.api.HelixParticipant;

import java.io.IOException;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class MkdirParticipant {

    public static void main(String args[]) {
        try {
            HelixParticipant<MkdirTask> participant = new HelixParticipant<>("application.properties", MkdirTask.class);
            new Thread(participant).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
