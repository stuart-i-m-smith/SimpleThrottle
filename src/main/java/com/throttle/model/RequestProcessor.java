package com.throttle.model;

public interface RequestProcessor {

    /**
     * To be called on receipt of a request
     *
     * @param client
     * @param request
     * @param status
     */
    void onRequest(String client, Object request, Status status);

}
