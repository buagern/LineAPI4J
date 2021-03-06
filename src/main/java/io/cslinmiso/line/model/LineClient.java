/**
 * 
 * @Package: io.cslinmiso.line.model
 * @FileName: LineClient.java
 * @author: treylin
 * @date: 2014/11/24, 下午 03:14:20
 * 
 * <pre>
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Trey Lin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *  </pre>
 */
package io.cslinmiso.line.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import line.thrift.Contact;
import line.thrift.ContentType;
import line.thrift.ErrorCode;
import line.thrift.Group;
import line.thrift.MIDType;
import line.thrift.Message;
import line.thrift.OpType;
import line.thrift.Operation;
import line.thrift.Profile;
import line.thrift.TMessageBox;
import line.thrift.TMessageBoxWrapUp;
import line.thrift.TMessageBoxWrapUpResponse;
import line.thrift.TalkException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import io.cslinmiso.line.api.LineApi;

public class LineClient {

  LineApi api;
  String _authToken;
  /** The revision. */
  public long revision = 0;

  Profile profile;
  List<LineContact> contacts;
  List<LineRoom> rooms;
  List<LineGroup> groups;

  public LineClient() throws Exception {
    throw new Exception("Please initialize LineClient with LineAPI.");
  }

  public LineClient(LineApi api) throws Exception {
    this.api = api;
    // 認證通過後登入
    String auth = api.loginWithVerifier();
    setAuthToken(auth); 
    // initialize
    this.setRevision(this.api.getLastOpRevision());
    this.getProfile();
    this.refreshGroups();
    this.refreshContacts();
    this.refreshActiveRooms();
  }


  /**
   * 用LINE ID搜尋並加入好友
   * 
   * @return
   * @throws Exception
   **/
  public Map<String, Contact> findAndAddContactsByUserid(String userid) throws Exception {
    return checkAuth() != true ? null : this.api.findAndAddContactsByUserid(0, userid);
  }

  /**
   * 用email搜尋並加入好友 (沒測試成功)
   * 
   * @return
   * @throws Exception
   **/
  public Map<String, Contact> findAndAddContactsByEmail(Set<String> emails) throws Exception {
    return checkAuth() != true ? null : this.api.findAndAddContactsByEmail(0, emails);
  }

  /**
   * 用電話搜尋並加入好友 (沒測試成功)
   * 
   * @return
   * @return
   * @throws Exception
   **/
  public Map<String, Contact> findAndAddContactsByPhone(Set<String> phone) throws Exception {
    return checkAuth() != true ? null : this.api.findAndAddContactsByPhone(0, phone);
  }

  public Profile getProfile() throws Exception {
    /**
     * Get profile information
     * 
     * returns Profile object; - picturePath - displayName - phone (base64 encoded?) -
     * allowSearchByUserid - pictureStatus - userid - mid # used for unique id for account -
     * phoneticName - regionCode - allowSearchByEmail - email - statusMessage
     **/

    /** Get `profile` of LINE account **/
    if (checkAuth()) {
      this.profile = this.api.getProfile();

      return profile;
    } else {
      return null;
    }
  }

  public LineContact getContactByName(String name) {
    for (LineContact contact : contacts) {
      if (name.equals(contact.getName())) {
        return contact;
      }
    }
    return null;
  }

  public LineContact getContactById(String id) {
    for (LineContact contact : contacts) {
      if (id.equals(contact.getId())) {
        return contact;
      }
    }
    return null;
  }

  public LineBase getContactOrRoomOrGroupById(String id) {
    // Get a `contact` or `room` or `group` by its id
    List<LineBase> list = new ArrayList<LineBase>();
    list.add(getContactById(id));
    list.add(getGroupById(id));
    list.add(getRoomById(id));
    list.removeAll(Collections.singleton(null)); // 移除null
    return list.size() >= 1 ? list.get(0) : null;
  }

  public void refreshGroups() throws Exception {
    // Refresh groups of LineClient
    // Refresh active chat rooms
    if (checkAuth()) {
      int start = 1;
      int count = 50;

      this.groups = new ArrayList<LineGroup>();
      List<String> groupIdsJoined =  this.api.getGroupIdsJoined();
      List<String> groupIdsInvited =  this.api.getGroupIdsInvited();
      
      addGroupsWithIds(groupIdsJoined);
      addGroupsWithIds(groupIdsInvited);
      
    }
  }

  public void addGroupsWithIds(List<String> groupIds) throws TalkException, TException, Exception {
    /** Refresh groups of LineClient */
    if (checkAuth()) {
      List<Group> newGroups = this.api.getGroups(groupIds);

      for (Group group : newGroups) {
        this.groups.add(new LineGroup(this, group));
      }
      // self.groups.sort()
    }
  }

  public void refreshContacts() throws TalkException, TException, Exception {
    /** Refresh contacts of LineClient **/
    if (checkAuth()) {
      List<String> contactIds = this.api.getAllContactIds();
      List<Contact> contacts = this.api.getContacts(contactIds);

      this.contacts = new ArrayList<LineContact>();

      for (Contact contact : contacts) {
        this.contacts.add(new LineContact(this, contact));
      }
    }
    // self.contacts.sort()
  }

  public List<LineContact> getHiddenContacts() throws Exception {
    // Refresh groups of LineClient
    if (checkAuth()) {
      List<String> contactIds = this.api.getBlockedContactIds();
      List<Contact> contacts = this.api.getContacts(contactIds);

      List<LineContact> c = new ArrayList<LineContact>();

      for (Contact contact : contacts) {
        c.add(new LineContact(this, contact));
      }
      return c;
    }
    return null;
  }

  public void refreshActiveRooms() throws TalkException, TException, Exception {
    // Refresh active chat rooms
    if (checkAuth()) {
      int start = 1;
      int count = 50;

      this.rooms = new ArrayList<LineRoom>();

      while (true) {
        TMessageBoxWrapUpResponse channel = this.api.getMessageBoxCompactWrapUpList(start, count);
        for (TMessageBoxWrapUp box : channel.messageBoxWrapUpList) {
          if (box.messageBox.midType == MIDType.ROOM) {
            LineRoom room = new LineRoom(this, this.api.getRoom(box.messageBox.id));
            this.rooms.add(room);
          }
        }
        if (channel.messageBoxWrapUpList.size() == count) {
          start += count;
        } else {
          break;
        }
      }
    }
  }


  public LineGroup createGroupWithIds(String name, List<String> ids) throws TalkException,
      TException, Exception {
    /**
     * Create a group with contact ids
     * 
     * :param name: name of group :param ids: list of contact ids
     **/
    if (checkAuth()) {

      LineGroup group = new LineGroup(this, this.api.createGroup(0, name, ids));
      this.groups.add(group);
      return group;
    }
    return null;
  }


  public LineGroup createGroupWithContacts(String name, List<LineContact> contacts)
      throws TalkException, TException, Exception {
    /*
     * Create a group with contacts
     * 
     * :param name: name of group :param contacts: list of contacts
     */
    if (checkAuth()) {

      List<String> contactIds = new ArrayList<String>();

      for (LineContact contact : contacts) {
        contactIds.add(contact.getId());
      }

      LineGroup group = new LineGroup(this, this.api.createGroup(0, name, contactIds));
      this.groups.add(group);

      return group;
    }
    return null;
  }


  public LineGroup getGroupByName(String name) {
    /**
     * Get a group by name
     * 
     * :param name: name of a group
     **/
    for (LineGroup group : this.groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }

    return null;

  }

  public LineGroup getGroupById(String id) {
    /*
     * Get a group by id
     * 
     * :param id: id of a group
     */

    for (LineGroup group : this.groups) {
      if (id.equals(group.getId())) {
        return group;
      }
    }

    return null;

  }

  public void inviteIntoGroup(LineGroup group, List<LineContact> contacts) throws Exception {
    /*
     * Invite contacts into group
     * 
     * :param group: LineGroup instance :param contacts: LineContact instances to invite
     */
    if (checkAuth()) {
      List<String> contactIds = this.api.getAllContactIds();

      for (LineContact contact : contacts) {
        contactIds.add(contact.getId());
      }
      this.api.inviteIntoGroup(0, group.getId(), contactIds);
    }
  }

  public boolean acceptGroupInvitation(LineGroup group) throws Exception {
    /**
     * Accept a group invitation
     * 
     * :param group: LineGroup instance
     **/
    if (checkAuth()) {

      this.api.acceptGroupInvitation(0, group.getId());
      return true;
    }
    return false;
  }

  public boolean leaveGroup(LineGroup group) throws Exception {
    /*
     * Leave a group
     * 
     * :param group: LineGroup instance to leave
     */
    if (checkAuth()) {
      this.api.leaveGroup(group.getId());
      return this.groups.remove(group);
    }
    return false;

  }

  public LineRoom createRoomWithIds(List<String> ids) throws TalkException, TException, Exception {
    /** Create a chat room with contact ids **/
    if (checkAuth()) {

      LineRoom room = new LineRoom(this, this.api.createRoom(ids.size(), ids));
      this.rooms.add(room);

      return room;
    }
    return null;

  }

  public LineRoom createRoomWithContacts(List<LineContact> contacts) throws TalkException,
      TException, Exception {
    /** Create a chat room with contacts **/
    if (checkAuth()) {
      List<String> contactIds = new ArrayList<String>();

      for (LineContact contact : contacts) {
        contactIds.add(contact.getId());
      }

      LineRoom room = new LineRoom(this, this.api.createRoom(contactIds.size(), contactIds));
      this.rooms.add(room);

      return room;
    }
    return null;

  }

  public LineRoom getRoomById(String id) {
    /**
     * Get a room by id
     * 
     * :param id: id of a room
     **/

    for (LineRoom room : this.rooms) {
      if (id.equals(room.getId())) {
        return room;
      }
    }
    return null;

  }

  public void inviteIntoRoom(LineRoom room, List<LineContact> contacts) throws TalkException,
      TException, Exception {
    /**
     * Invite contacts into room
     * 
     * :param room: LineRoom instance :param contacts: LineContact instances to invite
     **/
    if (checkAuth()) {
      List<String> contactIds = new ArrayList<String>();

      for (LineContact contact : contacts) {
        contactIds.add(contact.getId());
      }

      this.api.inviteIntoRoom(room.getId(), contactIds);
    }
  }

  public boolean leaveRoom(LineRoom room) throws TalkException, TException, Exception {
    /**
     * Leave a room
     * 
     * :param room: LineRoom instance to leave
     **/
    if (checkAuth()) {

      this.api.leaveRoom(room.getId());
      this.rooms.remove(room);

      return true;
    }
    return false;

  }

  public Message sendMessage(int seq, LineMessage message) throws TalkException, TException,
      Exception {
    /**
     * Send a message
     * 
     * :param message: LineMessage instance to send
     */
    if (checkAuth()) {
      // seq = 0;
      return this.api.sendMessage(seq, message);
    }
    return null;

  }

  public TMessageBox getMessageBox(String id) throws Exception {
    /**
     * Get MessageBox by id
     * 
     * :param id: `contact` id or `group` id or `room` id
     */
    if (checkAuth()) {

      TMessageBoxWrapUp messageBoxWrapUp = this.api.getMessageBoxCompactWrapUp(id);

      return messageBoxWrapUp.getMessageBox();
    }
    return null;

  }

  public List<LineMessage> getRecentMessages(TMessageBox messageBox, int count)
      throws TalkException, TException, Exception {
    /**
     * Get recent message from MessageBox
     * 
     * :param messageBox: MessageBox object
     */
    if (checkAuth()) {
      String id = messageBox.getId();
      List<Message> messages = this.api.getRecentMessages(id, count);

      return this.getLineMessageFromMessage(messages);
    }
    return null;
  }

  public void longPoll(int count) throws TalkException, TException, Exception {
    /**
     * Receive a list of operations that have to be processed by original Line cleint.
     * 
     * :param count: number of operations to get from :returns: a generator which returns operations
     * 
     * >>> for op in client.longPoll(){ sender = op[0] receiver = op[1] message = op[2] print
     * "%s->%s : %s" % (sender, receiver, message)
     */
    // count = 50;
    if (checkAuth()) {
      /* Check is there any operations from LINE server */

      List<Operation> operations = new ArrayList<Operation>();

      try {
        operations = this.api.fetchOperations(this.getRevision(), count);
      } catch (TalkException e) {
        if (ErrorCode.INVALID_MID == e.getCode()) {
          throw new Exception("user logged in to another machien");
        } else {
          return;
        }
      }catch (TTransportException e) {
        if (e.getMessage().indexOf("204") != -1) {
          return;
        } else {
          return;
        }
      }

      for (Operation operation : operations) {
        OpType opType = operation.getType();
        Message msg = operation.getMessage();
        if (opType == OpType.END_OF_OPERATION) {

        } else if (opType == OpType.SEND_MESSAGE) {

        } else if (opType == OpType.RECEIVE_MESSAGE) {
          LineMessage message = new LineMessage(this, msg);
          if(msg.getContentType() == ContentType.VIDEO || msg.getContentType() == ContentType.IMAGE){
            continue;
          }
          
          String id = null;
          String raw_mid = getProfile().getMid();
          String raw_sender = operation.getMessage().getFrom();
          String raw_receiver = operation.getMessage().getTo();

          // id = 實際發送者
          id = raw_receiver;
          if(raw_receiver.equals(raw_mid)){
            id=raw_sender;
          }
          
          LineBase sender = this.getContactOrRoomOrGroupById(raw_sender);
          LineBase receiver = this.getContactOrRoomOrGroupById(raw_receiver);

          if (sender == null || receiver == null) {
            this.refreshGroups();
            this.refreshContacts();
            this.refreshActiveRooms();

            sender = this.getContactOrRoomOrGroupById(raw_sender);
            receiver = this.getContactOrRoomOrGroupById(raw_receiver);

            System.out.printf("[*] sender: %s  receiver: %s\n", sender,receiver);
            // yield (sender, receiver, message);
          } else {
            System.out.printf("[*] %s\n", OpType.findByValue(operation.getType().getValue()));

          }


        }
        this.revision = Math.max(operation.getRevision(), this.revision);
      }


    }

  }

  public void createContactOrRoomOrGroupByMessage(Message message) {
    if (message.getToType() == MIDType.USER) {

    } else if (message.getToType() == MIDType.ROOM) {

    } else if (message.getToType() == MIDType.GROUP) {

    }
  }

  public List<LineMessage> getLineMessageFromMessage(List<Message> messages) {
    /*
     * Change Message objects to LineMessage objects
     * 
     * :param messges: list of Message object
     */
    List<LineMessage> lineMessages = new ArrayList<LineMessage>();

    for (Message message : messages) {
      lineMessages.add(new LineMessage(this, message));
    }
    return lineMessages;
  }

  public boolean checkAuth() throws Exception {
    /** Check if client is logged in or not **/
    if (this._authToken != null) {
      return true;
    } else {
      String msg = "you need to login";
      throw new Exception(msg);
    }
  }

  public LineApi getApi() {
    return api;
  }

  public void setApi(LineApi api) {
    this.api = api;
  }

  public String getAuthToken() {
    return _authToken;
  }

  public void setAuthToken(String _authToken) {
    this._authToken = _authToken;
  }

  public long getRevision() {
    return revision;
  }

  public void setRevision(long revision) {
    this.revision = revision;
  }

  public List<LineContact> getContacts() {
    return contacts;
  }

  public void setContacts(List<LineContact> contacts) {
    this.contacts = contacts;
  }

  public List<LineRoom> getRooms() {
    return rooms;
  }

  public void setRooms(List<LineRoom> rooms) {
    this.rooms = rooms;
  }

  public List<LineGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<LineGroup> groups) {
    this.groups = groups;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }


}
