package com.throttle.model;

public interface Gateway {

    /**
     * To be called before attempting to submit requests
     *
     * @param client
     */
    void connect(String client);

    /**
     * Submit a request.
     *
     * @param client
     * @param request
     * @return a Status of OK, if the request was accepted
     */
    Status submit(String client, Object request);

    /**
     * To be called when the client disconnects
     *
     * @param client
     */
    void disconnect(String client);
}