/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.dao.mock;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * Simple in-memory storage system for mock data access objects.
 */
public class MockDaoStorage {
    private static final Log log = LogFactory.getLog(MockDaoStorage.class);

    private HashMap<String, Item> itemsByPath;
    private HashMap<String, Item> itemsByUid;
    private HashMap<String, String> rootUids;
    private HashMap<String, Set> tickets;
    private SessionIdGenerator idGenerator;

    /** */
    public MockDaoStorage() {
        itemsByPath = new HashMap<String, Item>();
        itemsByUid = new HashMap<String, Item>();
        rootUids = new HashMap<String, String>();
        tickets = new HashMap<String, Set>();
        idGenerator = new SessionIdGenerator();
    }

    /** */
    public Item getItemByUid(String uid) {
        return itemsByUid.get(uid);
    }

    /** */

    public void setItemByUid(String uid, Item item) {
        itemsByUid.put(uid, item);
    }

    /** */
    public void removeItemByUid(String uid) {
        itemsByUid.remove(uid);
    }

    /** */
    public Item getItemByPath(String path) {
        return itemsByPath.get(path);
    }

    /** */
    public void setItemByPath(String path, Item item) {
        itemsByPath.put(path, item);
    }

    /** */
    public void removeItemByPath(String path) {
        itemsByPath.remove(path);
    }

    /** */
    public String getRootUid(String userId) {
        return rootUids.get(userId);
    }

    /** */
    public void setRootUid(String userId, String uid) {
        rootUids.put(userId, uid);
    }

    /** */
    public void removeRootUid(Long userId) {
        rootUids.remove(userId);
    }

    /** */
    public HomeCollectionItem getRootItem(String userId) {
        String rootUid = rootUids.get(userId);
        if (rootUid == null) {
            throw new IllegalStateException("user does not have a root item");
        }
        return (HomeCollectionItem) itemsByUid.get(rootUid);
    }

    /** */
    public HomeCollectionItem createRootItem(User user) {
        HomeCollectionItem rootCollection = new HomeCollectionItem();
        rootCollection.setName(user.getUsername());
        rootCollection.setOwner(user);
        rootCollection.setUid(calculateUid());
        rootCollection.setCreationDate(new Date());
        rootCollection.setModifiedDate(rootCollection.getCreationDate());

        itemsByUid.put(rootCollection.getUid(), rootCollection);
        itemsByPath.put("/" + rootCollection.getName(), rootCollection);
        rootUids.put(user.getUid(), rootCollection.getUid());

        return rootCollection;
    }

    /** */
    public void storeItem(Item item) {
        if (item.getName() == null)
            throw new IllegalArgumentException("name cannot be null");
       
        if (item.getOwner() == null)
            throw new IllegalArgumentException("owner cannot be null");

        if(item.getUid()==null)
            item.setUid(calculateUid());
        item.setCreationDate(new Date());
        item.setModifiedDate(item.getCreationDate());
        item.setEntityTag(item.calculateEntityTag());

        if(item.getParent()!=null) {
            for (Item sibling : item.getParent().getChildren()) {
                if (sibling.getName().equals(item.getName()))
                    throw new DuplicateItemNameException();
            }
            
            item.getParent().getChildren().add(item);
            itemsByPath.put(getItemPath(item.getParent()) + "/" + item.getName(),
                    item);
        }
        
        // handle NoteItem modifications
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                note.getModifies().addModification(note);
        }
        
        itemsByUid.put(item.getUid(), item);
    }

    /** */
    public void updateItem(Item item) {
        Item stored = itemsByUid.get(item.getUid());
        if (stored == null)
            throw new IllegalArgumentException("item to be updated is not already stored");
        if (! stored.equals(item))
            throw new IllegalArgumentException("item to be updated does not match stored item");
        if (item.getName() == null)
            throw new IllegalArgumentException("name cannot be null");
        if (item.getOwner() == null)
            throw new IllegalArgumentException("owner cannot be null");

        CollectionItem parentItem = item.getParent();

        if (parentItem != null) {
            for (Item sibling : parentItem.getChildren()) {
                if (sibling.getName().equals(item.getName()) &&
                    ! (sibling.getUid().equals(item.getUid())))
                    throw new DuplicateItemNameException();
            }
        }

        item.setModifiedDate(new Date());

        String path = "";
        if (parentItem != null)
            path += getItemPath(parentItem);
        path += "/" + item.getName();

        // XXX if the item name changed during the update, then we
        // leave a dangling map entry
        itemsByPath.put(path, item);
    }

    /** */
    public Set findItemChildren(Item item) {
        HashSet children = new HashSet();

        for (Item child : itemsByUid.values()) {
            if (child.getParent().getUid().equals(item.getUid())) {
                children.add(child);
            }
        }

        return Collections.unmodifiableSet(children);
    }

    /** */
    public Set<Ticket> findItemTickets(Item item) {
        Set<Ticket> itemTickets = (Set<Ticket>) tickets.get(item.getUid());
        if (itemTickets == null) {
            itemTickets = new HashSet<Ticket>();
            tickets.put(item.getUid(), itemTickets);
        }
        return itemTickets;
    }

    /** */
    public void createTicket(Item item, Ticket ticket) {
        ticket.setKey(calculateTicketKey());
        findItemTickets(item).add(ticket);
    }

    /** */
    public void removeTicket(Item item, Ticket ticket) {
        findItemTickets(item).remove(ticket);
    }

    /** */
    public String getItemPath(Item item) {
        StringBuffer path = new StringBuffer();
        LinkedList<String> hierarchy = new LinkedList<String>();
        hierarchy.addFirst(item.getName());

        Item currentItem = item;
        while (currentItem.getParent() != null) {
            currentItem = itemsByUid.get(currentItem.getParent().getUid());
            hierarchy.addFirst(currentItem.getName());
        }

        // hierarchy
        for (String part : hierarchy)
            path.append("/" + part);

        return path.toString();
    }

    /** */
    public String getItemPath(String uid) {
        return getItemPath(getItemByUid(uid));
    }

    public String calculateUid() {
        return idGenerator.nextStringIdentifier();
    }

    private String calculateTicketKey() {
        return idGenerator.nextStringIdentifier();
    }
}
