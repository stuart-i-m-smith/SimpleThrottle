package com.throttle;

import com.throttle.model.Status;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectionTest {

    @Test
    public void canTryRequestOk(){
        CountDownLatch requestIntervalExpiryLatch = new CountDownLatch(1);

        Connection connection = new Connection.Builder()
            .client("Client1")
            .maxRequestsPerInterval(1)
            .maxRequestsIntervalSeconds(1)
            .connectionRequestedCheckScheduler(new ConnectionRequestedCheckScheduler(2))
            .counterDecrementerFactory(new LatchedCounterDecrementerFactory(requestIntervalExpiryLatch))
            .build();

        assertThat(connection.tryRequest(), is(Status.OK));

        connection.disconnect();
    }

    @Test
    public void canTryRequestThrottle(){
        CountDownLatch requestIntervalExpiryLatch = new CountDownLatch(1);
        Connection connection = new Connection.Builder()
            .client("Client2")
            .maxRequestsIntervalSeconds(1)
            .connectionRequestedCheckScheduler(new ConnectionRequestedCheckScheduler(2))
            .counterDecrementerFactory(new LatchedCounterDecrementerFactory(requestIntervalExpiryLatch))
            .build();

        assertThat(connection.tryRequest(), is(Status.THROTTLED));

        connection.disconnect();
    }

    @Test
    public void canTryRequestOkThenThrottle(){
        CountDownLatch requestIntervalExpiryLatch = new CountDownLatch(1);
        Connection connection = new Connection.Builder()
            .client("Client3")
            .maxRequestsPerInterval(1)
            .maxRequestsIntervalSeconds(1)
            .connectionRequestedCheckScheduler(new ConnectionRequestedCheckScheduler(2))
            .counterDecrementerFactory(new LatchedCounterDecrementerFactory(requestIntervalExpiryLatch))
            .build();

        assertThat(connection.tryRequest(), is(Status.OK));
        assertThat(connection.tryRequest(), is(Status.THROTTLED));

        connection.disconnect();
    }


    @Test
    public void canTryRequestOkThenThrottleThenOk() throws InterruptedException {
        CountDownLatch requestIntervalExpiryLatch = new CountDownLatch(1);
        Connection connection = new Connection.Builder()
            .client("Client4")
            .maxRequestsPerInterval(1)
            .maxRequestsIntervalSeconds(1)
            .connectionRequestedCheckScheduler(new ConnectionRequestedCheckScheduler(2))
            .counterDecrementerFactory(new LatchedCounterDecrementerFactory(requestIntervalExpiryLatch))
            .build();

        assertThat(connection.tryRequest(), is(Status.OK));
        assertThat(connection.tryRequest(), is(Status.THROTTLED));

        assertTrue(requestIntervalExpiryLatch.await(2, TimeUnit.SECONDS));

        assertThat(connection.tryRequest(), is(Status.OK));

        connection.disconnect();
    }

    @Test
    public void canCheckClientRequestedAtStart() throws InterruptedException {
        CountDownLatch clientRequestedCheckLatch = new CountDownLatch(1);
        Connection connection = new Connection.Builder()
            .client("Client5")
            .maxRequestsPerInterval(1)
            .maxRequestsIntervalSeconds(1)
            .connectionRequestedCheckScheduler(new LatchedConnectionRequestedCheckScheduler(1, clientRequestedCheckLatch))
            .build();

        connection.scheduleClientRequestedCheck();

        assertTrue(clientRequestedCheckLatch.await(2, TimeUnit.SECONDS));

        connection.disconnect();
    }

    @Test
    public void canCheckClientRequestedAfterEachRequest() throws InterruptedException {
        CountDownLatch clientRequestedCheckLatch = new CountDownLatch(1);
        Connection connection = new Connection.Builder()
                .client("Client6")
                .maxRequestsPerInterval(1)
                .maxRequestsIntervalSeconds(1)
                .connectionRequestedCheckScheduler(new LatchedConnectionRequestedCheckScheduler(1, clientRequestedCheckLatch))
                .build();

        connection.tryRequest();

        assertTrue(clientRequestedCheckLatch.await(2, TimeUnit.SECONDS));

        connection.disconnect();
    }
}