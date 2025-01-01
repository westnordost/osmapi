package de.westnordost.osmapi.messages;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.user.User;

/** Parses a list of OpenStreetMap messages from OSM Messages API 0.6. It parses the XML naively,
 *  i.e. it does not care where in the XML the messages are. */
public class MessagesParser extends XmlParser implements ApiResponseReader<Void>
{
    private static final String
            MESSAGE = "message",
            TITLE = "title",
            BODY = "body";

    /* temporary map so we do not parse and hold many times the same user */
    private Map<Long, User> users;

    private final Handler<Message> handler;

    private Message message = null;

    public MessagesParser(Handler<Message> handler)
    {
        this.handler = handler;
    }

    @Override
    public Void parse(InputStream in) throws IOException
    {
        users = new HashMap<>();
        doParse(in);
        users = null;
        return null;
    }

    @Override
    protected void onStartElement() throws ParseException
    {
        String name = getName();

        if (MESSAGE.equals(name))
        {
            message = parseMessage();
        }
    }

    @Override
    protected void onEndElement() throws ParseException
    {
        String name = getName();

        if (MESSAGE.equals(name))
        {
            handler.handle(message);
            message = null;
        }
        else if (TITLE.equals(name))
        {
            message.title = getText();
        }
        else if (BODY.equals(name))
        {
            message.body = getText();
        }
    }

    private Message parseMessage() {
        Message message = new Message();
        message.id = getLongAttribute("id");
        message.sentOn = Instant.parse(getAttribute("sent_on"));
        message.messageRead = getBooleanAttribute("message_read");
        message.deleted = getBooleanAttribute("deleted");
        String bodyFormat = getAttribute("body_format");
        message.bodyFormat = Message.BodyFormat.valueOf(bodyFormat.toUpperCase(Locale.UK));
        message.fromUser = parseUser("from_user_id", "from_display_name");
        message.toUser = parseUser("to_user_id", "to_display_name");
        return message;
    }

    private User parseUser(String idKey, String nameKey)
    {
        Long userId = getLongAttribute(idKey);
        if(userId == null)
            return null;

        if(!users.containsKey(userId))
        {
            User user = new User(userId, getAttribute(nameKey));
            users.put(userId, user);
            return user;
        }
        return users.get(userId);
    }
}