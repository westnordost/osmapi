package de.westnordost.osmapi.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;

/** Send and receive messages */
public class MessagesApi {

    private static final String MESSAGES = "user/messages";

    private final OsmConnection osm;

    public MessagesApi(OsmConnection osm)
    {
        this.osm = osm;
    }

    /**
     * Retrieve messages from the user's inbox.
     *
     * @param handler The handler which is fed the incoming messages
     * @param limit number of messages returned at maximum. A number between 1 and 100 or null for
     *              server default.
     * @param order Messages are ordered by id in specified order. MessageOrder.NEWEST is used if
     *              not specified.
     * @param fromId The id of the first element in the returned list
     *
     * @throws OsmAuthorizationException if this application is not authorized to read the user's
     *                                   messages (Permission.CONSUME_MESSAGES)
     * @throws OsmQueryTooBigException if the allowed limit is exceeded
     * */
    public void getInbox(Handler<Message> handler, Integer limit, MessageOrder order, Long fromId)
    {
        Map<String, String> params = new HashMap<>();
        if (limit != null) params.put("limit", limit.toString());
        if (order != null) params.put("order", order.name().toLowerCase(Locale.UK));
        if (fromId != null) params.put("from_id", fromId.toString());
        String query = MESSAGES + "/inbox" + createQueryString(params);
        try
        {
            osm.makeAuthenticatedRequest(query, "GET", new MessagesParser(handler));
        }
        catch(OsmBadUserInputException e)
        {
            // we can be more specific here
            throw new OsmQueryTooBigException(e);
        }
    }

    /**
     * Retrieve messages from the user's inbox.
     *
     * @see #getInbox(Handler, Integer, MessageOrder, Long)
     * */
    public void getInbox(Handler<Message> handler, Integer limit)
    {
        getInbox(handler, limit, null, null);
    }

    /**
     * Retrieve messages from the user's inbox.
     *
     * @see #getInbox(Handler, Integer, MessageOrder, Long)
     * */
    public void getInbox(Handler<Message> handler)
    {
        getInbox(handler, null, null, null);
    }

    /**
     * Retrieve messages from the user's outbox (i.e. sent messages).
     *
     * @param handler The handler which is fed the incoming messages
     * @param limit number of messages returned at maximum. A number between 1 and 100 or null for
     *              server default.
     * @param order Messages are ordered by id in specified order. MessageOrder.NEWEST is used if
     *              not specified.
     * @param fromId The id of the first element in the returned list
     *
     * @throws OsmAuthorizationException if this application is not authorized to read the user's
     *                                   messages (Permission.CONSUME_MESSAGES)
     * @throws OsmQueryTooBigException if the allowed limit is exceeded
     * */
    public void getOutbox(Handler<Message> handler, Integer limit, MessageOrder order, Long fromId)
    {
        Map<String, String> params = new HashMap<>();
        if (limit != null) params.put("limit", limit.toString());
        if (order != null) params.put("order", order.name().toLowerCase(Locale.UK));
        if (fromId != null) params.put("from_id", fromId.toString());
        String query = MESSAGES + "/outbox" + createQueryString(params);
        try
        {
            osm.makeAuthenticatedRequest(query, "GET", new MessagesParser(handler));
        }
        catch(OsmBadUserInputException e)
        {
            // we can be more specific here
            throw new OsmQueryTooBigException(e);
        }
    }

    /**
     * Retrieve messages from the user's outbox (i.e. sent messages).
     *
     * @see #getOutbox(Handler, Integer, MessageOrder, Long)
     * */
    public void getOutbox(Handler<Message> handler, Integer limit)
    {
        getOutbox(handler, limit, null, null);
    }

    /**
     * Retrieve messages from the user's outbox (i.e. sent messages).
     *
     * @see #getOutbox(Handler, Integer, MessageOrder, Long)
     * */
    public void getOutbox(Handler<Message> handler)
    {
        getOutbox(handler, null, null, null);
    }

    /** Get the message with the given id.
     *
     * @param id message id
     * @return message. Null if it does not exist.
     *
     * @throws OsmAuthorizationException if this application is not authorized to read the user's
     *                                   messages (Permission.CONSUME_MESSAGES) or the message with
     *                                   the given id was neither sent by or received by the user.
     * */
    public Message get(long id)
    {
        SingleElementHandler<Message> handler = new SingleElementHandler<>();
        try
        {
            osm.makeAuthenticatedRequest(MESSAGES + "/" + id, "GET", new MessagesParser(handler));
        }
        catch(OsmNotFoundException e)
        {
            return null;
        }
        return handler.get();
    }

    /** Update the read status of the message with the given id.
     *
     * @param id message id
     * @param read whether the message should be marked as read
     *
     * @throws OsmAuthorizationException if this application is not authorized to read the user's
     *                                   messages (Permission.CONSUME_MESSAGES) or the message with
     *                                   the given id was not received by the user.
     * @throws OsmNotFoundException if the message with the given id does not exist.
     * */
    public void update(long id, boolean read)
    {
        osm.makeAuthenticatedRequest(MESSAGES + "/" + id + "?read_status=" + read, "PUT");
    }

    /** Delete the message with the given id.
     *
     * @param id message id
     *
     * @throws OsmAuthorizationException if this application is not authorized to send the user
     *                                   messages (Permission.SEND_MESSAGES) or the message with
     *                                   the given id was neither sent by or received by the user.
     * @throws OsmNotFoundException if the message with the given id does not exist.
     * */
    public void delete(long id)
    {
        osm.makeAuthenticatedRequest(MESSAGES + "/" + id, "DELETE");
    }

    /** Send a message to the given recipient
     *
     * @see #send(long, String, String, Message.BodyFormat)
     * */
    public void send(long recipientId, String title, String body)
    {
        send(recipientId, title, body, null);
    }

    /** Send a message to the given recipient
     *
     * @param recipientId user id of the recipient
     * @param title title of the message
     * @param body body of the message
     * @param format format of the body. May be null.
     *
     * @throws OsmAuthorizationException if this application is not authorized to send the user
     *                                   messages (Permission.SEND_MESSAGES) or the message with
     *                                   the given id was neither sent by or received by the user.
     * @throws OsmTooManyRequestsException when throttling was applied
     * */
    public void send(long recipientId, String title, String body, Message.BodyFormat format)
    {
        Map<String, String> params = new HashMap<>();
        params.put("recipient_id", "" + recipientId);
        params.put("title", title);
        params.put("body", body);
        if (format != null) params.put("format", format.name().toLowerCase(Locale.UK));

        osm.makeAuthenticatedRequest(MESSAGES + createQueryString(params), "POST");
    }

    /** Send a message to the given recipient
     *
     * @see #send(String, String, String, Message.BodyFormat)
     * */
    public void send(String recipientName, String title, String body)
    {
        send(recipientName, title, body, null);
    }

    /** Send a message to the given recipient
     *
     * @param recipientId user id of the recipient
     * @param title title of the message
     * @param body body of the message
     * @param format format of the body. May be null.
     *
     * @throws OsmAuthorizationException if this application is not authorized to send the user
     *                                   messages (Permission.SEND_MESSAGES) or the message with
     *                                   the given id was neither sent by or received by the user.
     * @throws OsmTooManyRequestsException when throttling was applied
     * */
    public void send(String recipientName, String title, String body, Message.BodyFormat format)
    {
        Map<String, String> params = new HashMap<>();
        params.put("recipient", recipientName);
        params.put("title", title);
        params.put("body", body);
        if (format != null) params.put("format", format.name().toLowerCase(Locale.UK));

        osm.makeAuthenticatedRequest(MESSAGES + createQueryString(params), "POST");
    }

    private String createQueryString(Map<String, String> params)
    {
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet())
        {
            if(first)
            {
                first = false;
                query.append("?");
            }
            else
            {
                query.append("&");
            }

            query.append(entry.getKey());
            query.append("=");
        }
        return query.toString();
    }
}
