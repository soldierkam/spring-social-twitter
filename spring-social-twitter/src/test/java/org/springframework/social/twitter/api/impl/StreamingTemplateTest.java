/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.twitter.api.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.RequestMatchers.*;
import static org.springframework.test.web.client.ResponseCreators.withResponse;

/**
 * @author Craig Walls
 */
public class StreamingTemplateTest extends AbstractTwitterApiTest {

    @Test
    @Ignore
    public void filter() throws Exception {
        mockServer.expect(requestTo("http://stream.twitter.com/1/statuses/filter.json"))
                .andExpect(method(POST))
                .andExpect(body("track=dallas"))
                .andRespond(withResponse(new ClassPathResource("filter-stream-track.json", getClass()), responseHeaders));

        MockStreamListener listener = new MockStreamListener(7) {
            protected void shutdown() {
                twitter.streamingOperations().stopStreaming();
            }
        };

        twitter.streamingOperations().filter("dallas", Arrays.<StreamListener>asList(listener));
        Thread.sleep(1000); // delay while stream is read
        assertEquals(4, listener.tweetsReceived.size());
        assertEquals(2, listener.deletesReceived.size());
        assertEquals(1, listener.limitsReceived.size());
    }

    @Test
    @Ignore
    public void filter_streamClosedByTwitter() throws Exception {
        for (int i = 0; i < 3; i++) { // expect the stream to be reopened 3 times
            mockServer.expect(requestTo("http://stream.twitter.com/1/statuses/filter.json"))
                    .andExpect(method(POST))
                    .andExpect(body("track=dallas"))
                    .andRespond(withResponse(new ClassPathResource("filter-stream-track.json", getClass()), responseHeaders));
        }

        MockStreamListener listener = new MockStreamListener(21) {
            protected void shutdown() {
                twitter.streamingOperations().stopStreaming();
            }
        };

        twitter.streamingOperations().filter("dallas", Arrays.<StreamListener>asList(listener));
        Thread.sleep(1000); // delay while stream is read
        assertEquals(12, listener.tweetsReceived.size());
        assertEquals(6, listener.deletesReceived.size());
        assertEquals(3, listener.limitsReceived.size());
    }

    private abstract static class MockStreamListener implements StreamListener {

        List<Tweet> tweetsReceived = new ArrayList<Tweet>();
        List<StreamDeleteEvent> deletesReceived = new ArrayList<StreamDeleteEvent>();
        List<Integer> limitsReceived = new ArrayList<Integer>();
        private int stopAfter = Integer.MAX_VALUE;

        public MockStreamListener(int stopAfter) {
            this.stopAfter = stopAfter;
        }

        public void onTweet(Tweet tweet) {
            tweetsReceived.add(tweet);
            messageReceived();
        }

        public void onDelete(StreamDeleteEvent deleteEvent) {
            deletesReceived.add(deleteEvent);
            messageReceived();
        }

        public void onLimit(int numberOfLimitedTweets) {
            limitsReceived.add(numberOfLimitedTweets);
            messageReceived();
        }

        private void messageReceived() {
            stopAfter--;
            if (stopAfter == 0) {
                shutdown();
            }
        }

        protected abstract void shutdown();
    }
}
