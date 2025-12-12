package de.westnordost.osmapi.user;

/** Link to the profile page of a user on a social platform. */
public class UserSocialLink implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** The social platform. Might be null or empty if not detected. */
    public String platform;

    /** The URL */
    public String url;
}