package graph;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    // Primary constructor initializing the message payload from text
    public Message(String text) {
        this.asText = text;
        this.data = text != null ? text.getBytes() : new byte[0];
        this.date = new Date();
        
        double tempDouble;
        try {
            tempDouble = Double.parseDouble(text);
        } catch (NumberFormatException | NullPointerException e) {
            tempDouble = Double.NaN;
        }
        this.asDouble = tempDouble;
    }

    // Secondary constructor converting byte arrays into a text message
    public Message(byte[] data) {
        this(data != null ? new String(data) : "");
    }

    // Secondary constructor converting primitive doubles into a text message
    public Message(double value) {
        this(Double.toString(value));
    }
}