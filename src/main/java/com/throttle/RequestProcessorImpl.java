package com.throttle;

import com.throttle.model.RequestProcessor;
import com.throttle.model.Status;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessorImpl implements RequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onRequest(String client, Object request, Status status) {
        LOGGER.info("Client <{}> Requested <{}> - Status <{}>", client, request, status);
    }
}
