/*
 * Copyright 2010 the original author or authors.
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

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.oauth1.AbstractOAuth1ApiBinding;
import org.springframework.social.twitter.api.*;
import org.springframework.web.client.RestOperations;

import java.util.List;

/**
 * This is the central class for interacting with Twitter.
 * <p>
 * Most (not all) Twitter operations require OAuth authentication. To perform
 * such operations, {@link TwitterTemplate} must be constructed with the minimal
 * amount of information required to sign requests to Twitter's API with an
 * OAuth <code>Authorization</code> header.
 * </p>
 * <p>
 * There are a few operations, such as searching, that do not require OAuth
 * authentication. In those cases, you may use a {@link TwitterTemplate} that is
 * created through the default constructor and without any OAuth details.
 * Attempts to perform secured operations through such an instance, however,
 * will result in {@link NotAuthorizedException} being thrown.
 * </p>
 *
 * @author Craig Walls
 */
public class TwitterTemplate extends AbstractOAuth1ApiBinding implements Twitter {

    private TimelineOperations timelineOperations;

    private UserOperations userOperations;

    private FriendOperations friendOperations;

    private ListOperations listOperations;

    private SearchOperations searchOperations;

    private DirectMessageOperations directMessageOperations;

    private BlockOperations blockOperations;

    private GeoOperations geoOperations;

    private StreamingOperations streamOperations;

    /**
     * Create a new instance of TwitterTemplate.
     * This constructor creates a new TwitterTemplate able to perform unauthenticated operations against Twitter's API.
     * Some operations, such as search, do not require OAuth authentication.
     * A TwitterTemplate created with this constructor will support those operations.
     * Those operations requiring authentication will throw {@link NotAuthorizedException}.
     */
    public TwitterTemplate() {
        super();
        registerTwitterJsonModule();
        getRestTemplate().setErrorHandler(new TwitterErrorHandler());
        initSubApis();
    }

    /**
     * Create a new instance of TwitterTemplate.
     *
     * @param consumerKey       the application's API key
     * @param consumerSecret    the application's API secret
     * @param accessToken       an access token acquired through OAuth authentication with Twitter
     * @param accessTokenSecret an access token secret acquired through OAuth authentication with Twitter
     */
    public TwitterTemplate(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        super(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        registerTwitterJsonModule();
        getRestTemplate().setErrorHandler(new TwitterErrorHandler());
        initSubApis();
    }

    public TimelineOperations timelineOperations() {
        return timelineOperations;
    }

    public FriendOperations friendOperations() {
        return friendOperations;
    }

    public ListOperations listOperations() {
        return listOperations;
    }

    public SearchOperations searchOperations() {
        return searchOperations;
    }

    public DirectMessageOperations directMessageOperations() {
        return directMessageOperations;
    }

    public UserOperations userOperations() {
        return userOperations;
    }

    public BlockOperations blockOperations() {
        return blockOperations;
    }

    public GeoOperations geoOperations() {
        return geoOperations;
    }

    public StreamingOperations streamingOperations() {
        return streamOperations;
    }

    public RestOperations restOperations() {
        return getRestTemplate();
    }
    // private helper

    private void registerTwitterJsonModule() {
        List<HttpMessageConverter<?>> converters = getRestTemplate().getMessageConverters();
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJacksonHttpMessageConverter) {
                MappingJacksonHttpMessageConverter jsonConverter = (MappingJacksonHttpMessageConverter) converter;
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new TwitterModule());
                jsonConverter.setObjectMapper(objectMapper);
            }
        }
    }

    private void initSubApis() {
        this.userOperations = new UserTemplate(getRestTemplate(), isAuthorized());
        this.directMessageOperations = new DirectMessageTemplate(getRestTemplate(), isAuthorized());
        this.friendOperations = new FriendTemplate(getRestTemplate(), isAuthorized());
        this.listOperations = new ListTemplate(getRestTemplate(), isAuthorized());
        this.timelineOperations = new TimelineTemplate(getRestTemplate(), isAuthorized());
        this.searchOperations = new SearchTemplate(getRestTemplate(), isAuthorized());
        this.blockOperations = new BlockTemplate(getRestTemplate(), isAuthorized());
        this.geoOperations = new GeoTemplate(getRestTemplate(), isAuthorized());
        this.streamOperations = new StreamingTemplate(getRestTemplate(), isAuthorized());
    }

}
