package com.oosic.iot.controller.library;

import com.oosic.iot.controller.R;

public enum IotCommandType {

   LOCAL_BROADCAST, 
   LOCAL_PEER_TO_PEER, 
   REMOTE_SERVER;

   public static boolean isLocalBroadcast(IotCommandType type) {
      return LOCAL_BROADCAST.compareTo(type) == 0;
   }

   public static boolean isLocalPeerToPeer(IotCommandType type) {
      return LOCAL_PEER_TO_PEER.compareTo(type) == 0;
   }

   public static boolean isRemoteServer(IotCommandType type) {
      return REMOTE_SERVER.compareTo(type) == 0;
   }
   
   public static int getDescription(IotCommandType type) {
      if (type == LOCAL_BROADCAST) {
         return R.string.local_broadcast;
      } else if (type == LOCAL_PEER_TO_PEER) {
         return R.string.local_peer_to_peer;
      } else if (type == REMOTE_SERVER) {
         return R.string.remote_server;
      } else {
         return R.string.unknown_type;
      }
   }

}
