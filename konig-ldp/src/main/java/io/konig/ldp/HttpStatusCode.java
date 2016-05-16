package io.konig.ldp;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
 * %%
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
 * #L%
 */


/*
 * Modified from from org.apache.commons.httpclient.HttpStatus
 */

public class HttpStatusCode {
	  // -------------------------------------------------------------- Constants

    // --- 1xx Informational ---

    /** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
    public static final int CONTINUE = 100;
    /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616)*/
    public static final int SWITCHING_PROTOCOLS = 101;
    /** <tt>102 Processing</tt> (WebDAV - RFC 2518) */
    public static final int PROCESSING = 102;

    // --- 2xx Success ---

    /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
    public static final int OK = 200;
    /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
    public static final int CREATED = 201;
    /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
    public static final int ACCEPTED = 202;
    /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
    public static final int NO_CONTENT = 204;
    /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
    public static final int RESET_CONTENT = 205;
    /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
    public static final int PARTIAL_CONTENT = 206;
    /** 
     * <tt>207 Multi-Status</tt> (WebDAV - RFC 2518) or <tt>207 Partial Update
     * OK</tt> (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
     */
    public static final int MULTI_STATUS = 207;

    // --- 3xx Redirection ---

    /** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
    public static final int MULTIPLE_CHOICES = 300;
    /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
    public static final int MOVED_PERMANENTLY = 301;
    /** <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC 1945) */
    public static final int MOVED_TEMPORARILY = 302;
    /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
    public static final int SEE_OTHER = 303;
    /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
    public static final int NOT_MODIFIED = 304;
    /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
    public static final int USE_PROXY = 305;
    /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
    public static final int TEMPORARY_REDIRECT = 307;

    // --- 4xx Client Error ---

    /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
    public static final int BAD_REQUEST = 400;
    /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
    public static final int UNAUTHORIZED = 401;
    /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
    public static final int PAYMENT_REQUIRED = 402;
    /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
    public static final int FORBIDDEN = 403;
    /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
    public static final int NOT_FOUND = 404;
    /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int METHOD_NOT_ALLOWED = 405;
    /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
    public static final int NOT_ACCEPTABLE = 406;
    /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616)*/
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
    public static final int REQUEST_TIMEOUT = 408;
    /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
    public static final int CONFLICT = 409;
    /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
    public static final int GONE = 410;
    /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
    public static final int LENGTH_REQUIRED = 411;
    /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int PRECONDITION_FAILED = 412;
    /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
    public static final int REQUEST_TOO_LONG = 413;
    /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
    public static final int REQUEST_URI_TOO_LONG = 414;
    /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int EXPECTATION_FAILED = 417;

    /**
     * Static constant for a 418 error.
     * <tt>418 Unprocessable Entity</tt> (WebDAV drafts?)
     * or <tt>418 Reauthentication Required</tt> (HTTP/1.1 drafts?)
     */
    // not used
    // public static final int UNPROCESSABLE_ENTITY = 418;

    /**
     * Static constant for a 419 error.
     * <tt>419 Insufficient Space on Resource</tt>
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     * or <tt>419 Proxy Reauthentication Required</tt>
     * (HTTP/1.1 drafts?)
     */
    public static final int INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    /**
     * Static constant for a 420 error.
     * <tt>420 Method Failure</tt>
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     */
    public static final int METHOD_FAILURE = 420;
    /** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 2518) */
    public static final int UNPROCESSABLE_ENTITY = 422;
    /** <tt>423 Locked</tt> (WebDAV - RFC 2518) */
    public static final int LOCKED = 423;
    /** <tt>424 Failed Dependency</tt> (WebDAV - RFC 2518) */
    public static final int FAILED_DEPENDENCY = 424;

    // --- 5xx Server Error ---

    /** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
    public static final int INTERNAL_SERVER_ERROR = 500;
    /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
    public static final int NOT_IMPLEMENTED = 501;
    /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
    public static final int BAD_GATEWAY = 502;
    /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
    public static final int SERVICE_UNAVAILABLE = 503;
    /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
    public static final int GATEWAY_TIMEOUT = 504;
    /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    /** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 2518) */
    public static final int INSUFFICIENT_STORAGE = 507;

}
