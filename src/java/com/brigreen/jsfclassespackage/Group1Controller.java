package com.brigreen.jsfclassespackage;

import com.brigreen.planneroftheapes.Group1;
import com.brigreen.jsfclassespackage.util.JsfUtil;
import com.brigreen.jsfclassespackage.util.JsfUtil.PersistAction;
import com.brigreen.planneroftheapes.User;
import com.brigreen.sessionbeanpackage.Group1Facade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@Named("group1Controller")
@SessionScoped
public class Group1Controller implements Serializable {

    @EJB
    private com.brigreen.sessionbeanpackage.Group1Facade ejbFacade;
    private List<Group1> items = null;
    private Group1 selected;
    private User user;
    private int counter = 20;

    public Group1Controller() {
    }

    public Group1 getSelected() {
        return selected;
    }

    public void setSelected(Group1 selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private Group1Facade getFacade() {
        return ejbFacade;
    }

    public Group1 prepareCreate() {
        selected = new Group1();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("Group1Created"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }
    
    public void createGroup()
    {
        List<Group1> list = this.getGroupListFromUserID(user.getId());
        for(Group1 g : list)
        {
            if (g.getName().equals(selected.getName()) && g.getAdmin().equals(user.getPid()))
            {
                JsfUtil.addErrorMessage("You cannot have two of your groups have the same name");
                return;
            }
        }
        selected.setAdmin(user.getPid());
        selected.setUserID(user.getId());
        selected.setId(counter);
        selected.setGroupID(14); //temporary group ID number
        selected.setGroupUserid(counter++); //temporary group user ID
        create();
        list = this.getGroupListFromUserID(user.getId());
        for (Group1 g : list) {
            if (g.getName().equals(selected.getName()) && g.getAdmin().equals(user.getPid())) {
                selected = g;
                selected.setGroupID(selected.getId()); //Fix group ID number
                selected.setGroupUserid(selected.getId()); //Fix group User Id
                update();
                return;
            }
        }
    }
    
    /**
     * Adds a member to a group if you have the right permissions
     * @param newUser The current user.
     */
    public void addMember(User newUser)
    {
        List<Group1> list = this.getGroupListFromUserID(newUser.getId());
        for (Group1 g : list) {
            if (g.getGroupID() == selected.getGroupID()) {
                JsfUtil.addErrorMessage("User is already a member of this group");
                return;
            }
        }
        Group1 newGroup = new Group1();
        initializeEmbeddableKey();
        newGroup.setAdmin(selected.getAdmin());
        newGroup.setName(selected.getName());
        newGroup.setGroupID(selected.getGroupID());
        newGroup.setId(counter);
        newGroup.setGroupUserid(counter++);
        newGroup.setUserID(newUser.getId());
        selected = newGroup;
        create();
    }
    /**
     * The Post action to remove a user from a group. Removes the group entry
     * in the database.
     * @param groupid Takes the group id of the entry.
     */
    public void removeMemberPost(int groupid)
    {
        if (groupid != selected.getId())
        {
            JsfUtil.addErrorMessage("Cannot Remove Member from Group");
        }
        else
        {
            destroy();
        }
    }
    
    /**
     * The Pre action to remove a user from a group, passes the return to the 
     * assignment controller to remove assignments for the group entry before
     * removing the group entry.
     * @param currentUser The currently logged on user
     * @return The id of the group entry.
     */
    public int removeMemberPre(User currentUser)
    {
        if(currentUser.getPid().equals(selected.getAdmin()))
        {
            if(selected.getUserID() != currentUser.getId())
            {
                return selected.getId();
            }
            else
            {
                JsfUtil.addErrorMessage("You can't remove yourself, you are the group admin.");
            }
        }
        else
        {
            JsfUtil.addErrorMessage("You're not the group admin.");
        }
        return 0;

    }
    
    /**
     * Delete Group Post call - removes all group entries for a group
     * @param list List of group entries to remove.
     */
    public void deleteGroupPost(List<Group1> list)
    {
        if (list == null)
        {
            JsfUtil.addErrorMessage("Cannot Delete Group");
        }
        else
        {
            for(Group1 item : list)
            {
                selected = item;
                destroy();
            }
        }
    }
    
    /**
     * Delete Group Pre Action - Finds all the group entries that need to be 
     * deleted to remove the group and then returns that value so the
     * assignment controller can remove the associated assignments.
     * @param currentUser The currently logged on user
     * @return List of group entries to be deleted
     */
    public List<Group1> deleteGroupPre(User currentUser)
    {
        if(currentUser.getPid().equals(selected.getAdmin()))
        {
            return this.getFacade().findByQueryOneParam("SELECT a FROM Group1 a WHERE a.groupID LIKE :ID", "ID", selected.getGroupID());
        }
        else
        {
            JsfUtil.addErrorMessage("You're not the group admin.");
        }
        return null;
    }
    
    /**
     * Removes the current user from a particular group.
     * @param theUser The currently logged on user
     */
    public void leaveGroup(User theUser)
    {
        if(selected.getUserID() == theUser.getId() && !selected.getAdmin().equals(theUser.getPid()))
        {
            //Not Admin and selected yourself in the group.
            destroy();
        }
        else if(selected.getAdmin().equals(theUser.getPid()))
        {
            JsfUtil.addErrorMessage("You cannot leave a group you are an admin of.");
        }
        else if(selected.getUserID() == theUser.getId())
        {
            JsfUtil.addErrorMessage("You are not the selected user.");
        }
        
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("Group1Updated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("Group1Deleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Group1> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Group1 getGroup1(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Group1> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Group1> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
    
    /**
     * Returns Group Name from groupid number
     * @param groupId Group id number
     * @return Group Name
     */
    public String getGroupNameFromID(int groupId) 
    {
        List<Group1> myItems = getFacade().findByQueryOneParam("SELECT a FROM Group1 a WHERE a.groupID LIKE :ID", "ID", groupId);
        items = myItems;
        return myItems.get(0).getName();
    }
    
    /**
     * Returns a list of Group entries for a particular user
     * @param userId The user id
     * @return A list of group entries for that user
     */
    public List<Group1> getGroupInfoFromUserID(int userId) 
    {
        List<Group1> myItems = getFacade().findByQueryOneParam("SELECT a FROM Group1 a WHERE a.userID LIKE :ID", "ID", userId);
        items = myItems;
        List<Group1> groupinfo = new ArrayList<Group1>();

        for (Group1 group: myItems)
        {
            groupinfo.addAll(getFacade().findByQueryOneParam("SELECT a FROM Group1 a WHERE a.groupID LIKE :ID", "ID", group.getGroupID()));
        }
        return groupinfo;
    }

    /**
     * Returns the group entries for a particular user.
     * @param userId The user id of the current user
     * @return  List of group entries for that user.
     */
    public List<Group1> getGroupListFromUserID(int userId) 
    {
        List<Group1> myItems = getFacade().findByQueryOneParam("SELECT a FROM Group1 a WHERE a.userID LIKE :ID", "ID", userId);
        items = myItems;
        return items;
    }
    
    @FacesConverter(forClass = Group1.class)
    public static class Group1ControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            Group1Controller controller = (Group1Controller) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "group1Controller");
            return controller.getGroup1(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Group1) {
                Group1 o = (Group1) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Group1.class.getName()});
                return null;
            }
        }

    }
    
public void setUser(User user)
{
    this.user = user;
}
}
