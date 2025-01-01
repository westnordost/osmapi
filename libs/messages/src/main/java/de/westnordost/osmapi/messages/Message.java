package de.westnordost.osmapi.messages;

import java.time.Instant;

import de.westnordost.osmapi.user.User;

/** An OpenStreetMap message */
public class Message
{
    public Long id;
    public User fromUser;
    public User toUser;
    public String title;
    public String body;
    public BodyFormat bodyFormat;
    public Instant sentOn;
    public boolean messageRead;
    public boolean deleted;

    public enum BodyFormat {
        TEXT,
        MARKDOWN,
        HTML
    }
}