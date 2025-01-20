package de.westnordost.osmapi.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.user.UserApi;
import de.westnordost.osmapi.user.UserDetails;

public class MessagesApiTest
{
    private MessagesApi privilegedApi;
    private MessagesApi anonymousApi;
    private MessagesApi unprivilegedApi;
    private User user;

    @Before public void setUp()
    {
        OsmConnection privilegedConnection = ConnectionTestFactory.createConnection(
                ConnectionTestFactory.User.ALLOW_EVERYTHING
        );
        anonymousApi = new MessagesApi(ConnectionTestFactory.createConnection(null));
        privilegedApi = new MessagesApi(privilegedConnection);
        unprivilegedApi = new MessagesApi(ConnectionTestFactory.createConnection(
                ConnectionTestFactory.User.ALLOW_NOTHING));
        UserDetails details = new UserApi(privilegedConnection).getMine();
        user = new User(details.id, details.displayName);
    }

    @Test public void anonymous()
    {
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.getInbox(new NullHandler<>()));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.getOutbox(new NullHandler<>()));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.get(1));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.update(1, true));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.delete(1));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.send(1, "T", "B"));
        assertThrows(OsmAuthorizationException.class, () -> anonymousApi.send("U", "T", "B"));
    }

    @Test public void unprivileged()
    {
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.getInbox(new NullHandler<>()));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.getOutbox(new NullHandler<>()));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.get(1));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.update(1, true));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.delete(1));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.send(1, "T", "B"));
        assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.send("U", "T", "B"));
    }

    @Test public void failsOnLimitExceeded()
    {
        assertThrows(
                OsmQueryTooBigException.class,
                () -> privilegedApi.getOutbox(new NullHandler<>(), Integer.MAX_VALUE)
        );
        assertThrows(
                OsmQueryTooBigException.class,
                () -> privilegedApi.getInbox(new NullHandler<>(), Integer.MAX_VALUE)
        );
    }

    @Test public void failsUpdateOnNotFound()
    {
        assertThrows(OsmNotFoundException.class, () -> privilegedApi.update(0, true));
        assertThrows(OsmNotFoundException.class, () -> privilegedApi.delete(0));
    }

    @Test public void returnsNullForNonExistingMessage()
    {
        assertNull(privilegedApi.get(0L));
    }

    @Test public void sendToNonExistingRecipient()
    {
        assertThrows(
                OsmNotFoundException.class,
                () -> privilegedApi.send(0L, "To non-existing recipient", "A test")
        );
        assertThrows(
                OsmBadUserInputException.class,
                () -> privilegedApi.send("Surely this user does not exist!!!", "To non-existing recipient", "Test")
        );
    }

    @Test public void sendByName()
    {
        Message message = privilegedApi.send(user.displayName, "Note to myself", "Ho ho ho");
        privilegedApi.delete(message.id);
    }

    @Test public void sendGetReadDelete()
    {
        // send
        Message sentMessage = privilegedApi.send(user.id, "Note to myself", "Hey hey hey");
        assertNotNull(sentMessage);
        Predicate<Message> findMessage = (message) -> sentMessage.id == message.id;

        try {
            // get outbox
            FindSingleElementHandler<Message> outbox = new FindSingleElementHandler<>(findMessage);
            privilegedApi.getOutbox(outbox);
            Message outboxMessage = outbox.get();
            assertNotNull(outboxMessage);
            assertFalse(outboxMessage.read);
            assertFalse(outboxMessage.deleted);

            // get inbox
            FindSingleElementHandler<Message> inbox = new FindSingleElementHandler<>(findMessage);
            privilegedApi.getInbox(inbox);
            Message inboxMessage = inbox.get();
            assertNotNull(inboxMessage);
            assertFalse(inboxMessage.read);
            assertFalse(inboxMessage.deleted);

            // get by id
            Message message = privilegedApi.get(inboxMessage.id);
            assertNotNull(message);

            assertEquals(message.fromUser.id, user.id);
            assertEquals(message.fromUser.displayName, user.displayName);
            assertEquals(message.toUser.id, user.id);
            assertEquals(message.toUser.displayName, user.displayName);
            assertEquals(message.title, "Note to myself");
            assertEquals(message.body, "Hey hey hey");
            assertEquals(message.bodyFormat, Message.BodyFormat.MARKDOWN);
            assertFalse(message.read);
            assertFalse(message.deleted);

            assertMessagesEqual(message, inboxMessage);
            assertMessagesEqual(message, outboxMessage);
            assertMessagesEqual(message, sentMessage);
            assertEquals(message.body, sentMessage.body);

            // mark read
            Message readMessage = privilegedApi.update(message.id, true);
            assertNotNull(readMessage);
            assertTrue(readMessage.read);

        } finally {
            // mark deleted
            Message deletedMessage = privilegedApi.delete(sentMessage.id);
            assertNotNull(deletedMessage);
            assertTrue(deletedMessage.deleted);
        }
    }

    private static void assertMessagesEqual(Message a, Message b) {
        assertEquals(a.fromUser.id, b.fromUser.id);
        assertEquals(a.fromUser.displayName, b.fromUser.displayName);
        assertEquals(a.toUser.id, b.toUser.id);
        assertEquals(a.toUser.displayName, b.toUser.displayName);
        assertEquals(a.title, b.title);
        assertEquals(a.bodyFormat, b.bodyFormat);
        assertEquals(a.sentOn, b.sentOn);
        assertEquals(a.read, b.read);
        assertEquals(a.deleted, b.deleted);
        assertEquals(a.id, b.id);
        // not the body
    }

    private static class NullHandler<T> implements Handler<T>
    {
        public void handle(T tea) { }
    }

    private static class FindSingleElementHandler<T> implements Handler<T>
    {
        private final Predicate<T> predicate;
        private T element = null;

        public FindSingleElementHandler(Predicate<T> predicate)
        {
            this.predicate = predicate;
        }

        public void handle(T tea)
        {
            if (predicate.test(tea)) element = tea;
        }

        public T get()
        {
            return element;
        }
    }
}
