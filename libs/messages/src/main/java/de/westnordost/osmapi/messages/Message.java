package de.westnordost.osmapi.messages;

import java.io.Serializable;
import java.time.Instant;

import de.westnordost.osmapi.user.User;

/** An OpenStreetMap message */
public class Message implements Serializable
{
    private static final long serialVersionUID = 1L;

    public long id;
    public User fromUser;
    public User toUser;
    public String title;
    public String body;
    public BodyFormat bodyFormat;
    public Instant sentOn;
    /** Whether the message has been read. Only non-null for messages sent to the current user */
    public Boolean read;
    public boolean deleted;

    public enum BodyFormat {
        TEXT,
        MARKDOWN,
        HTML
    }
}