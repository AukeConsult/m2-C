/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.general;
public enum ReturMessageTypes {
	ok, general, message_error, no_pingalive, host_unknown, service_stopped, service_not_ready, service_not_running, service_not_connected, recieve_timeout, send_timeout, no_peerid, peer_sending, peer_timeout, peer_not_found, peer_unknown_port, peer_is_empty, peer_is_me, peer_is_closed, peer_session_stopped, sending_error, channel_io, channel_cipher, channel_message_parsing, wrongtype, socket_error, socket_closed, socket_got_wrong_message, send_socket_closed, empty_data, socket_port_exists, socket_wrong_clientid, send_socket_open, data_exceed_size, socket_no_clientid, session_timeout, peer_connect_timeout, peer_ka_request_error, peer_connect_error, ready_for_lookup, no_license, no_middleman_avail, peer_is_stopped, peer_session_not_found, no_session_encryption, service_is_restarting, send_no_session,
}