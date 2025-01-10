package de.westnordost.osmapi.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

public class MessagesParserTest {
    @Test public void basicElements() throws IOException
    {
        String xml =
                "<message id=\"777\" from_user_id=\"111\" to_user_id=\"222\" deleted=\"false\" sent_on=\"2019-06-15T08:26:04Z\">" +
                "</message>";

        Message message = parseOne(xml);
        assertEquals(777L, message.id);
        assertNull(message.read);
        assertFalse(message.deleted);
        assertEquals(Instant.parse("2019-06-15T08:26:04Z"), message.sentOn);
        assertEquals(222, message.toUser.id);
        assertEquals(111, message.fromUser.id);
    }

    @Test public void optionalElements() throws IOException
    {
        String xml =
                "<message id=\"777\" from_user_id=\"111\" from_display_name=\"X\" to_user_id=\"222\" to_display_name=\"Y\" sent_on=\"2019-06-15T08:26:04Z\" message_read=\"true\" deleted=\"true\" body_format=\"markdown\">" +
                "  <title>Title</title>" +
                "  <body>Body</body>" +
                "</message>";

        Message message = parseOne(xml);
        assertEquals(Message.BodyFormat.MARKDOWN, message.bodyFormat);
        assertEquals("Y", message.toUser.displayName);
        assertEquals("X", message.fromUser.displayName);
        assertTrue(message.read);
        assertTrue(message.deleted);
        assertEquals("Title", message.title);
        assertEquals("Body", message.body);
    }

    @Test public void parseMultiple() throws IOException
    {
        String xml =
                "<message id=\"1\" from_user_id=\"2\" to_user_id=\"3\" sent_on=\"2019-06-15T08:26:04Z\" message_read=\"false\" deleted=\"false\"/>" +
                "<message id=\"2\" from_user_id=\"2\" to_user_id=\"3\" sent_on=\"2019-06-15T08:26:04Z\" message_read=\"false\" deleted=\"false\"/>" +
                "<message id=\"3\" from_user_id=\"2\" to_user_id=\"3\" sent_on=\"2019-06-15T08:26:04Z\" message_read=\"false\" deleted=\"false\"/>";

        ListHandler<Message> handler = new ListHandler<>();
        new MessagesParser(handler).parse(TestUtils.asInputStream(xml));
        assertEquals(3, handler.get().size());
    }

    private Message parseOne(String xml) throws IOException
    {
        SingleElementHandler<Message> handler = new SingleElementHandler<>();
        new MessagesParser(handler).parse(TestUtils.asInputStream(xml));
        return handler.get();
    }
}