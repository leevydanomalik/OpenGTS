package org.opengts.war.track.page;

import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONParsingException;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;
import org.opengts.db.dmtp.*;
import org.opengts.war.tools.*;
import org.opengts.war.track.*;

public class UGroupInfo 
	extends WebPageAdaptor 
	implements Constants {

    // ------------------------------------------------------------------------
    // Parameters

    // forms 
    public  static final String FORM_GROUP_SELECT           = "GroupInfoSelect";
    public  static final String FORM_GROUP_EDIT             = "GroupInfoEdit";
    public  static final String FORM_GROUP_NEW              = "GroupInfoNew";
    public  static final String FORM_PROPERTY_EDIT          = "GroupPropEdit";

    // commands
    public  static final String COMMAND_INFO_UPD_GROUP      = "updateGrp";
    public  static final String COMMAND_INFO_UPD_PROPS      = "updateProps";
    public  static final String COMMAND_INFO_SELECT         = "selectGrp";
    public  static final String COMMAND_INFO_NEW            = "new";

    // submit
    public  static final String PARM_SUBMIT_EDIT            = "g_subedit";
    public  static final String PARM_SUBMIT_VIEW            = "g_subview";
    public  static final String PARM_SUBMIT_CHG             = "g_subchg";
    public  static final String PARM_SUBMIT_DEL             = "g_subdel";
    public  static final String PARM_SUBMIT_NEW             = "g_subnew";
    public  static final String PARM_SUBMIT_QUE             = "g_subque";
    public  static final String PARM_SUBMIT_PROP            = "g_subprop";

    // buttons
    public  static final String PARM_BUTTON_CANCEL          = "g_btncan";
    public  static final String PARM_BUTTON_BACK            = "g_btnbak";
    public  static final String PARM_BUTTON_ADD_DEV         = "g_btnadddev";    
    public  static final String PARM_BUTTON_REMOVE_DEV      = "g_btnremovedev";    

    // parameters
    public  static final String PARM_NEW_NAME               = "g_newname";
    public  static final String PARM_GROUP_SELECT           = "g_group";
    public  static final String PARM_GROUP_DESC             = "g_desc";
    public  static final String PARM_GROUP_MEMBERS          = "g_members";

    // tables
    public  static final String PARM_TABLE_ACC_DEV          = "t_acctdev";
    public  static final String PARM_TABLE_GRP_DEV          = "t_grpdev";
    
    // device properties
    public  static final String PARM_PROP_START_TYPE        = "p_startTyp";
    public  static final String PARM_PROP_START_DEF         = "p_startDef";
    public  static final String PARM_PROP_MOT_INTERV        = "p_motion";
    public  static final String PARM_PROP_STOP_TYPE         = "p_stopTyp";
    public  static final String PARM_PROP_STOP_INTERV       = "p_stopIntv";
    public  static final String PARM_PROP_DORM_INTERV       = "p_dormIntv";
    public  static final String PARM_PROP_DORM_COUNT        = "p_dormCnt";
    public  static final String PARM_PROP_EXCESS_SPEED      = "p_overSpeed";

    // ------------------------------------------------------------------------
    // CSS Class "class='"
    
    public  static final String CSS_DEVICES_VIEW            = "groupDevicesViewDiv";
    public  static final String CSS_DEVICES_HEADER_ROW      = "groupDevicesHeaderRow";
    public  static final String CSS_DEVICES_HEADER_COL      = "groupDevicesHeaderCol";
    public  static final String CSS_DEVICES_DATA_ROW_ODD    = "groupDevicesDataRowOdd";
    public  static final String CSS_DEVICES_DATA_ROW_EVN    = "groupDevicesDataRowEvn";
    public  static final String CSS_DEVICES_DATA_COL        = "groupDevicesDataCol";

    // ------------------------------------------------------------------------
    // Account chooser    
    private static final String  ID_ACCOUNT_ID              = "accountSelector";
    private static final String  ID_ACCOUNT_DESCR           = "accountDescription";
    private static final String  ID_ACCOUNT_DD				= "accountDropDown";
    private static final String  ACC_DEV_SEP				= "#";
//    public  static final String  PARM_ACCOUNT_SELECT        = "p_account";
    
    // ------------------------------------------------------------------------
    // WebPage interface
    
    public UGroupInfo()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_UGROUP_INFO);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_PASSWD, PAGE_MENU_TOP, PAGE_ZONE_INFO, PAGE_SYSADMIN_ACCOUNTS, PAGE_ACCOUNT_INFO, PAGE_USER_INFO, PAGE_UGROUP_INFO, PAGE_DEVICE_INFO, PAGE_MAP_DEVICE, PAGE_MAP_FLEET, PAGE_MAP_FLEETLIVE });
        this.setLoginRequired(true);
    }

    // ------------------------------------------------------------------------
   
    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_ADMIN;
    }

    public String getMenuDescription(RequestProperties reqState, String parentMenuName)
    {
        String grpTitles[]   = reqState.getDeviceGroupTitles();
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(UGroupInfo.class);
        return super._getMenuDescription(reqState,i18n.getString("UGroupInfo.editMenuDesc","View/Edit {0} Information", grpTitles));
    }
   
    public String getMenuHelp(RequestProperties reqState, String parentMenuName)
    {
        String grpTitles[] = reqState.getDeviceGroupTitles();
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(UGroupInfo.class);
        return super._getMenuHelp(reqState,i18n.getString("UGroupInfo.editMenuHelp","View and Edit {0} information", grpTitles));
    }

    // ------------------------------------------------------------------------

    public String getNavigationDescription(RequestProperties reqState)
    {
        String grpTitles[] = reqState.getDeviceGroupTitles();
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(UGroupInfo.class);
        return super._getNavigationDescription(reqState,i18n.getString("UGroupInfo.navDesc","{0}", grpTitles));
    }

    public String getNavigationTab(RequestProperties reqState)
    {
        String grpTitles[] = reqState.getDeviceGroupTitles();
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(UGroupInfo.class);
        return super._getNavigationTab(reqState,i18n.getString("UGroupInfo.navTab","{0} Admin", grpTitles));
    }

    // ------------------------------------------------------------------------
    
    /* encode a PendingPacket.SetPropertyPacket, and add it to the property change list */
    private boolean _addPropertyPacket(HttpServletRequest request, String reqAttr, int propCode, 
        java.util.List<PendingPacket.SetPropertyPacket> propList)
    {
        String val = AttributeTools.getRequestString(request, reqAttr, "");
        if ((val != null) && !val.equals("") && !val.equals("?")) {
            PendingPacket.SetPropertyPacket spp = PendingPacket.createSetPropertyPacket(propCode, val);
            if (spp != null) {
                propList.add(spp);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /* update Device table with user entered information */
    private String _queueDeviceGroupProperties(DeviceGroup selGroup, User userAuth, HttpServletRequest request, I18N i18n)
    {
        // 'selGroup' is not null
        String msg = null;

        /* retrieve user entered property changes */
        java.util.List<PendingPacket.SetPropertyPacket> propList = new Vector<PendingPacket.SetPropertyPacket>();
        // PropertyKey.PROP_MOTION_START_TYPE
        if (!this._addPropertyPacket(request,PARM_PROP_START_TYPE,PropertyKey.PROP_MOTION_START_TYPE,propList)) {
            msg = i18n.getString("GroupInfo.invalidStartType","Invalid 'Start Type' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_START
        if (!this._addPropertyPacket(request,PARM_PROP_START_DEF,PropertyKey.PROP_MOTION_START,propList)) {
            msg = i18n.getString("GroupInfo.invalidStartDefinition","Invalid 'Start Definition' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_IN_MOTION
        if (!this._addPropertyPacket(request,PARM_PROP_MOT_INTERV,PropertyKey.PROP_MOTION_IN_MOTION,propList)) {
            msg = i18n.getString("GroupInfo.invalidInMotion","Invalid 'In-Motion Interval' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_STOP_TYPE
        if (!this._addPropertyPacket(request,PARM_PROP_STOP_TYPE,PropertyKey.PROP_MOTION_STOP_TYPE,propList)) {
            msg = i18n.getString("GroupInfo.invalidStopType","Invalid 'Stop Type' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_STOP
        if (!this._addPropertyPacket(request,PARM_PROP_STOP_INTERV,PropertyKey.PROP_MOTION_STOP,propList)) {
            msg = i18n.getString("GroupInfo.invalidStopInterval","Invalid 'Stop Interval' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_DORMANT_INTRVL
        if (!this._addPropertyPacket(request,PARM_PROP_DORM_INTERV,PropertyKey.PROP_MOTION_DORMANT_INTRVL,propList)) {
            msg = i18n.getString("GroupInfo.invalidDormantInterval","Invalid 'Dormant Interval' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_DORMANT_COUNT
        if (!this._addPropertyPacket(request,PARM_PROP_DORM_COUNT,PropertyKey.PROP_MOTION_DORMANT_COUNT,propList)) {
            msg = i18n.getString("GroupInfo.invalidDormantCount","Invalid 'Dormant Count' property"); // UserErrMsg
            return msg;
        }
        // PropertyKey.PROP_MOTION_EXCESS_SPEED
        if (!this._addPropertyPacket(request,PARM_PROP_EXCESS_SPEED,PropertyKey.PROP_MOTION_EXCESS_SPEED,propList)) {
            msg = i18n.getString("GroupInfo.invalidExcessSpeed","Invalid 'Excess Speed' property"); // UserErrMsg
            return msg;
        }
        
        /* no properties defined? */
        if (propList.isEmpty()) {
            msg = i18n.getString("GroupInfo.noPropertiesChanged","No property changes have been defined"); // UserErrMsg
            return msg;
        }

        /* get devices for group */
        OrderedSet<String> devList = null;
        try {
            devList = selGroup.getDevices(userAuth,true/*inclInactv*/);
            if (ListTools.isEmpty(devList)) {
                return i18n.getString("UGroupInfo.noDevicesInGroup","No authorized devices in this group"); // UserErrMsg
            }
        } catch (DBException dbe) {
            Print.logException("Reading Devices", dbe);
            return i18n.getString("UGroupInfo.errorReadingDevices","Internal Devices error"); // UserErrMsg
        }

        /* loop through devices */
        int queueCount = 0;
        for (int d = 0; d < devList.size(); d++) {
            try {

                /* get Device */
                Account account = selGroup.getAccount();
                Device device = Device._getDevice(account, devList.get(d));
                if ((device == null) || !device.getDataTransport().getSupportsDMTP()) {
                    continue;
                }

                /* loop through properties */
                for (Iterator i = propList.iterator(); i.hasNext();) {
                    PendingPacket.SetPropertyPacket spp = (PendingPacket.SetPropertyPacket)i.next();
                    if (PendingPacket.insertSetPropertyPacket(device, spp)) {
                        queueCount++;
                    }
                }
    
            } catch (DBException dbe) {

                Print.logException("Error inserting PendingPacket ...", dbe);
                msg = i18n.getString("GroupInfo.errorPendingPacket","Internal PendingPacket error"); // UserErrMsg
                break;

            }

        }
    
        /* return result */
        if (msg != null) {
            return  msg;
        } else
        if (queueCount == 0) {
            return i18n.getString("GroupInfo.noPropertiesQueued","No property changes have been queued"); // UserErrMsg
        } else {
            return i18n.getString("GroupInfo.setPropertiesQueued","Requested property changes have been queued"); // UserErrMsg
        }

    }

    private OrderedSet<String[]> _decodeJSONGrp(String groupMembers, String currAcctId, boolean enableUniversalGroups)
    {
        String aId;
        String dId;
        OrderedSet<String[]> devList = new OrderedSet<String[]>();
        
		JSON._Object objJsonMember = null;
		JSON._Array arrJsonMembers = null;
        if (groupMembers != null) {
        	try {
        		arrJsonMembers = JSON.parse_Array(groupMembers);
        		if(arrJsonMembers!=null) {
        			Integer size = arrJsonMembers.size();
        			for(int i=0; i<size; i++) {
        				objJsonMember = arrJsonMembers.getObjectValueAt(i,null);
        				if(objJsonMember!=null) {
        					aId = objJsonMember.getStringForName("accountID", ""); 
        					dId = objJsonMember.getStringForName("deviceID", "");
        					if(!enableUniversalGroups && !aId.equals(currAcctId)) continue; 
        			    	String[] device = new String[] { aId, dId };
        			        devList.add(device);
        				}
        			}
        		}
			} catch (JSONParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	return devList;
    }

    // ------------------------------------------------------------------------
    
    public void writePage(
        final RequestProperties reqState,
        String pageMsg)
        throws IOException
    {
        final HttpServletRequest request = reqState.getHttpServletRequest();
        final PrivateLabel privLabel   = reqState.getPrivateLabel();
        final I18N         i18n        = privLabel.getI18N(GroupInfo.class);
        final Locale       locale      = reqState.getLocale();
        final Account      currAcct    = reqState.getCurrentAccount(); // never null
        final User         currUser    = reqState.getCurrentUser(); // may be null
        final String       pageName    = this.getPageName();
        final String       grpTitles[] = reqState.getDeviceGroupTitles();
        final boolean 	   enableUniversalGroups = privLabel.getBooleanProperty(PrivateLabel.PROP_TrackMap_enableUniversalGroups,false);
        String m = pageMsg;
        boolean error = false;

        /* argument group-id */
        OrderedSet<String> groupList = reqState.getDeviceGroupIDList(true); // non-null, length > 0
        String selGroupID = AttributeTools.getRequestString(reqState.getHttpServletRequest(), PARM_GROUP_SELECT, "");
        //if (selGroupID.equals("")) {
        //    selGroupID = groupList.get(0);
        //}

        /* contains group "ALL"? */
        boolean allGroupsAllowed = groupList.contains(DeviceGroup.DEVICE_GROUP_ALL);

        /* authorized selected group? */
        if (!StringTools.isBlank(selGroupID) && !groupList.contains(selGroupID)) {
            selGroupID = "";
        }

        /* DeviceGroup db */
        DeviceGroup selGroup = null;
        try {
            selGroup = !selGroupID.equals("")? DeviceGroup.getDeviceGroup(currAcct,selGroupID) : null; // may still be null
        } catch (DBException dbe) {
            // ignore
        }

        /* ACL allow view/new/delete */
        boolean allowNew     = privLabel.hasAllAccess(currUser, this.getAclName()) && allGroupsAllowed;
        boolean allowDelete  = allowNew;
        boolean allowEdit    = allowNew  || privLabel.hasWriteAccess(currUser, this.getAclName());
        boolean allowView    = allowEdit || privLabel.hasReadAccess(currUser, this.getAclName());
        boolean allowProp    = allowView && privLabel.getBooleanProperty(PrivateLabel.PROP_GroupInfo_showPropertiesButton,false);

        /* submit buttons */
        String  submitEdit   = AttributeTools.getRequestString(request, PARM_SUBMIT_EDIT, "");
        String  submitView   = AttributeTools.getRequestString(request, PARM_SUBMIT_VIEW, "");
        String  submitChange = AttributeTools.getRequestString(request, PARM_SUBMIT_CHG , "");
        String  submitNew    = AttributeTools.getRequestString(request, PARM_SUBMIT_NEW , "");
        String  submitDelete = AttributeTools.getRequestString(request, PARM_SUBMIT_DEL , "");
        String  submitQueue  = AttributeTools.getRequestString(request, PARM_SUBMIT_QUE , "");
        String  submitProps  = AttributeTools.getRequestString(request, PARM_SUBMIT_PROP, "");

        /* command */
        String  groupCmd     = reqState.getCommandName();
        boolean selectGroup  = groupCmd.equals(COMMAND_INFO_SELECT);
        boolean newGroup     = groupCmd.equals(COMMAND_INFO_NEW);
        boolean updateGroup  = groupCmd.equals(COMMAND_INFO_UPD_GROUP);
        boolean updateProps  = groupCmd.equals(COMMAND_INFO_UPD_PROPS);
        boolean deleteGroup  = false;

        /* ui display */
        boolean uiList       = false;
        boolean uiEdit       = false;
        boolean uiView       = false;
        boolean uiProp       = false;

        /* pre-qualify commands */
        String newGroupID = null;
        if (newGroup) {
            if (!allowNew) {
                newGroup = false; // not authorized
            } else {
                HttpServletRequest httpReq = reqState.getHttpServletRequest();
                newGroupID = AttributeTools.getRequestString(httpReq,PARM_NEW_NAME,"").trim();
                newGroupID = newGroupID.toLowerCase();
                if (StringTools.isBlank(newGroupID)) {
                    m = i18n.getString("GroupInfo.enterNewGroup","Please enter a new {0} ID.", grpTitles); // UserErrMsg
                    error = true;
                    newGroup = false;
                } else
                if (!WebPageAdaptor.isValidID(reqState,/*PrivateLabel.PROP_GroupInfo_validateNewIDs,*/newGroupID)) {
                    m = i18n.getString("GroupInfo.invalidIDChar","ID contains invalid characters"); // UserErrMsg
                    error = true;
                    newGroup = false;
                }
            }
        } else
        if (updateGroup) {
            if (!allowEdit) {
                updateGroup = false; // not authorized
            } else
            if (!SubmitMatch(submitChange,i18n.getString("GroupInfo.change","Change"))) {
                updateGroup = false;
            } else
            if (selGroup == null) {
                // should not occur
                m = i18n.getString("GroupInfo.unableToUpdate","Unable to update Group, ID not found"); // UserErrMsg
                error = true;
                updateGroup = false;
            }
        } else
        if (updateProps) {
            if (!allowProp) {
                updateProps = false; // not authorized
            } else
            if (selGroup == null) {
                m = i18n.getString("GroupInfo.pleaseSelectGroup","Please select a {0}", grpTitles); // UserErrMsg
                error = true;
                updateProps = false; // not selected
            } else
            if (!SubmitMatch(submitQueue,i18n.getString("GroupInfo.queue","Queue"))) {
                updateProps = false;
            }
        } else
        if (selectGroup) {
            if (SubmitMatch(submitDelete,i18n.getString("GroupInfo.delete","Delete"))) {
                if (!allowDelete) {
                    deleteGroup = false; // not authorized
                } else
                if (selGroup == null) {
                    m = i18n.getString("GroupInfo.pleaseSelectGroup","Please select a {0}", grpTitles); // UserErrMsg
                    error = true;
                    deleteGroup = false; // not selected
                } else {
                    deleteGroup = true;
                }
            } else
            if (SubmitMatch(submitEdit,i18n.getString("GroupInfo.edit","Edit"))) {
                if (!allowEdit) {
                    uiEdit = false; // not authorized
                } else
                if (selGroup == null) {
                    m = i18n.getString("GroupInfo.pleaseSelectGroup","Please select a {0}", grpTitles); // UserErrMsg
                    error = true;
                    uiEdit = false; // not selected
                } else {
                    uiEdit = true;
                }
            } else
            if (SubmitMatch(submitView,i18n.getString("GroupInfo.view","View"))) {
                if (!allowView) {
                    uiView = false; // not authorized
                } else
                if (selGroup == null) {
                    m = i18n.getString("GroupInfo.pleaseSelectGroup","Please select a {0}", grpTitles); // UserErrMsg
                    error = true;
                    uiView = false; // not selected
                } else {
                    uiView = true;
                }
            } else
            if (SubmitMatch(submitProps,i18n.getString("GroupInfo.properties","Properties"))) {
                if (!allowProp) {
                    uiProp = false; // not authorized
                } else
                if (selGroup == null) {
                    m = i18n.getString("GroupInfo.pleaseSelectGroup","Please select a {0}", grpTitles); // UserErrMsg
                    error = true;
                    uiProp = false; // not selected
                } else {
                    uiProp = true;
                }
            }
        }

        /* delete group? */
        if (deleteGroup) {
            // 'selGroup' guaranteed non-null here
            try {
                DeviceGroup.Key groupKey = (DeviceGroup.Key)selGroup.getRecordKey();
                Print.logWarn("Deleting DeviceGroup: " + groupKey);
                groupKey.delete(true); // will also delete dependencies
                selGroupID = "";
                selGroup = null;
                reqState.clearDeviceGroupList();
                // select another group
                groupList = reqState.getDeviceGroupIDList(true); // non-null, length > 0
                if (!ListTools.isEmpty(groupList)) {
                    selGroupID = groupList.get(0);
                    try {
                        selGroup = !selGroupID.equals("")? DeviceGroup.getDeviceGroup(currAcct, selGroupID) : null; // may still be null
                    } catch (DBException dbe) {
                        // ignore
                    }
                }
            } catch (DBException dbe) {
                Print.logException("Deleting DeviceGroup", dbe);
                m = i18n.getString("GroupInfo.errorDelete","Internal error deleting {0}", grpTitles); // UserErrMsg
                error = true;
            }
            uiList = true;
        }

        /* update the device info? */
        if (newGroup) {
            boolean createGroupOK = true;
            for (int u = 0; u < groupList.size(); u++) {
                if (newGroupID.equalsIgnoreCase(groupList.get(u))) {
                    m = i18n.getString("GroupInfo.alreadyExists","This {0} ID already exists", grpTitles); // UserErrMsg
                    error = true;
                    createGroupOK = false;
                    break;
                }
            }
            if (createGroupOK) {
                try {
                    DeviceGroup group = DeviceGroup.createNewDeviceGroup(currAcct, newGroupID);
                    reqState.clearDeviceGroupList();
                    groupList = reqState.getDeviceGroupIDList(true);
                    selGroup = group;
                    selGroupID = group.getGroupID();
                    Print.logInfo("Created group '%s'", selGroupID);
                    m = i18n.getString("GroupInfo.createdGroup","New {0} has been created", grpTitles); // UserErrMsg
                } catch (DBException dbe) {
                    Print.logException("Creating DeviceGroup", dbe);
                    m = i18n.getString("GroupInfo.errorCreate","Internal error creating {0}", grpTitles); // UserErrMsg
                    error = true;
                }
            }
            uiList = true;
        }

        /* update the group info? */
        if (updateGroup) {
            // 'selGroup' guaranteed non-null here
            selGroup.clearChanged();
            try {
                boolean saveOK = true;
                // description
                String groupDesc = AttributeTools.getRequestString(request, PARM_GROUP_DESC, "");
                if (!groupDesc.equals("")) {
                    selGroup.setDescription(groupDesc);
                }
                // members
                String groupMembers = AttributeTools.getRequestString(request, PARM_GROUP_MEMBERS, "");
                if (!groupMembers.equals("")) {
                	OrderedSet<String[]> devList = _decodeJSONGrp(groupMembers, selGroup.getAccountID(), enableUniversalGroups);
                	if(enableUniversalGroups) selGroup.setMembers(devList); 
                	else selGroup.setMembersID(devList);
                }
                // save
                if (saveOK) {
                    selGroup.save();
                    m = i18n.getString("GroupInfo.groupUpdated","{0} information updated", grpTitles); // UserErrMsg
                } else {
                    // should stay on this page
                }
            } catch (Throwable t) {
                Print.logException("Updating DeviceGroup", t);
                m = i18n.getString("GroupInfo.errorUpdating","Internal error updating {0}", grpTitles); // UserErrMsg
                error = true;
            }
            uiList = true;
        }

        /* update properties */
        if (updateProps) {
            // 'selGroup' guaranteed non-null here
            m = _queueDeviceGroupProperties(selGroup, currUser, request, i18n);
            Print.logInfo("Returned Message: " + m);
            uiList = true;
        }

        /* Style */
        HTMLOutput HTML_CSS = new HTMLOutput() {
            public void write(PrintWriter out) throws IOException {
                String cssDir = UGroupInfo.this.getCssDirectory();
                WebPageAdaptor.writeCssLink(out, reqState, "GroupInfo.css", cssDir);
                if (AccountChooser.isAccountChooserUseTable(privLabel)) {
                    AccountChooser.writeStyle(out, reqState);
                }
                
            }
        };

        /* JavaScript */
        HTMLOutput HTML_JS = new HTMLOutput() {
            public void write(PrintWriter out) throws IOException {
                MenuBar.writeJavaScript(out, pageName, reqState);
                JavaScriptTools.writeJSInclude(out, JavaScriptTools.qualifyJSFileRef(SORTTABLE_JS), request);
                AccountChooser.writeJavaScript(out, locale, reqState, "");
                JavaScriptTools.writeJSInclude(out, JavaScriptTools.qualifyJSFileRef("UGroupInfo.js"), request);
            }
        };

        /* Content */
        final OrderedSet<String> _groupList = groupList;
        final DeviceGroup _selGroup = selGroup;
        final String  _selGroupID  = selGroupID;
        final boolean _allowEdit   = allowEdit;
        final boolean _allowView   = allowView;
        final boolean _allowDelete = allowDelete;
        final boolean _allowNew    = allowNew;
        final boolean _allowProp   = allowProp;
        final boolean _uiProp      = _allowProp && uiProp;
        final boolean _uiEdit      = _allowEdit && uiEdit;
        final boolean _uiView      = _uiEdit || uiView;
        final boolean _uiList      = uiList || (!_uiEdit && !_uiView && !_uiProp);
        HTMLOutput HTML_CONTENT = null;
        if (_uiList) {

            HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
                public void write(PrintWriter out) throws IOException {
                    String pageName = UGroupInfo.this.getPageName();
                    String grpTitles[] = reqState.getDeviceGroupTitles();
                    String devTitles[] = reqState.getDeviceTitles();
    
                    // frame header
                  //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                    String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                    String editURL    = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String selectURL  = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String newURL     = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String frameTitle = _allowEdit? 
                        i18n.getString("GroupInfo.viewEditGroup","View/Edit {0} Information", grpTitles) : 
                        i18n.getString("GroupInfo.viewGroup","View {0} Information", grpTitles);
                    out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                    out.write("<hr>\n");
                        
                    // group selection table (Select, Group ID, Group Description, Device Count)
                    out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("GroupInfo.selectGroup","Select a {0}",grpTitles)+":</h1>\n");
                    out.write("<div style='margin-left:25px;'>\n");
                    out.write("<form name='"+FORM_GROUP_SELECT+"' method='post' action='"+selectURL+"' target='_self'>"); // target='_top'
                    out.write("<input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_SELECT+"'/>");
                    out.write("<table class='"+CommonServlet.CSS_ADMIN_SELECT_TABLE+"' cellspacing=0 cellpadding=0 border=0>\n");
                    out.write(" <thead>\n");
                    out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_ROW+"'>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL_SEL+"' nowrap>"+i18n.getString("GroupInfo.select","Select")+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("GroupInfo.groupID","{0} ID",grpTitles)+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("GroupInfo.groupName","{0} Name",grpTitles)+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("GroupInfo.deviceCount","{0} Count",devTitles)+"</th>\n");
                    out.write("  </tr>\n");
                    out.write(" </thead>\n");
                    out.write(" <tbody>\n");
                    for (int u = 0; u < _groupList.size(); u++) {
                        String grid = _groupList.get(u);
                        //Print.logInfo("Group ID: " + grid);
                        if ((u & 1) == 0) {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_ODD+"'>\n");
                        } else {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_EVEN+"'>\n");
                        }
                        if (grid.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                            String groupID   = FilterText(DeviceGroup.DEVICE_GROUP_ALL);
                            String groupDesc = FilterText(reqState.getDeviceGroupDescription(DeviceGroup.DEVICE_GROUP_ALL,false/*!rtnDispName*/));
                            String devCount  = (currAcct != null)? String.valueOf(currAcct.getDeviceCount()) : "n/a";
                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+u+"'>--</td>\n");
                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+groupID+"</td>\n");
                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+groupDesc+"</td>\n");
                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+devCount+"</td>\n");
                        } else {
                            try {
                                DeviceGroup grp = DeviceGroup.getDeviceGroup(currAcct, grid);
                                if (grp != null) {
                                    String groupID   = FilterText(grp.getGroupID());
                                    String groupDesc = FilterText(grp.getDescription());
                                    String devCount  = String.valueOf(grp.getDeviceCount());
                                    String checked   = _selGroupID.equals(grp.getGroupID())? "checked" : "";
                                    out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+u+"'><input type='radio' name='"+PARM_GROUP_SELECT+"' id='"+groupID+"' value='"+groupID+"' "+checked+"></td>\n");
                                    out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap><label for='"+groupID+"'>"+groupID+"</label></td>\n");
                                    out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+groupDesc+"</td>\n");
                                    out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+devCount+"</td>\n");
                                }
                            } catch (DBException dbe) {
                                // 
                            }
                        }
                        out.write("  </tr>\n");
                    }
                    out.write(" </tbody>\n");
                    out.write("</table>\n");
                    out.write("<table cellpadding='0' cellspacing='0' border='0' style='width:95%; margin-top:5px; margin-left:5px; margin-bottom:5px;'>\n");
                    out.write("<tr>\n");
                    if (_allowView  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_VIEW+"' value='"+i18n.getString("GroupInfo.view","View")+"'>");
                        out.write("</td>\n"); 
                    }
                    if (_allowEdit  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_EDIT+"' value='"+i18n.getString("GroupInfo.edit","Edit")+"'>");
                        out.write("</td>\n"); 
                    }
                    if (_allowProp  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_PROP+"' value='"+i18n.getString("GroupInfo.properties","Properties")+"'>");
                        out.write("</td>\n"); 
                    }
                    out.write("<td style='width:100%; text-align:right; padding-right:10px;'>");
                    if (_allowDelete) { 
                        out.write("<input type='submit' name='"+PARM_SUBMIT_DEL+"' value='"+i18n.getString("GroupInfo.delete","Delete")+"' "+Onclick_ConfirmDelete(locale)+">");
                    } else {
                        out.write("&nbsp;"); 
                    }
                    out.write("</td>\n");
                    out.write("</tr>\n");
                    out.write("</table>\n");
                    out.write("</form>\n");
                    out.write("</div>\n");
                    out.write("<hr>\n");
                    
                    /* new group */
                    if (_allowNew) {
                    out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("GroupInfo.createNewGroup","Create a new {0}",grpTitles)+":</h1>\n");
                    out.write("<div style='margin-top:5px; margin-left:5px; margin-bottom:5px;'>\n");
                    out.write("<form name='"+FORM_GROUP_NEW+"' method='post' action='"+newURL+"' target='_self'>"); // target='_top'
                    out.write(" <input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_NEW+"'/>");
                    out.write(i18n.getString("GroupInfo.groupID","{0} ID",grpTitles)+": <input type='text' class='"+CommonServlet.CSS_TEXT_INPUT+"' name='"+PARM_NEW_NAME+"' value='' size='32' maxlength='32'><br>\n");
                    out.write(" <input type='submit' name='"+PARM_SUBMIT_NEW+"' value='"+i18n.getString("GroupInfo.new","New")+"' style='margin-top:5px; margin-left:10px;'>\n");
                    out.write("</form>\n");
                    out.write("</div>\n");
                    out.write("<hr>\n");
                    }

                }
            };
            
        } else
        if (_uiEdit || _uiView) {

            final boolean _editGroup  = _uiEdit;
            final boolean _viewDevice = _editGroup || _uiView;
            HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
                public void write(PrintWriter out) throws IOException {
                    String pageName = UGroupInfo.this.getPageName();
                    String grpTitles[] = reqState.getDeviceGroupTitles();
                    String devTitles[] = reqState.getDeviceTitles();
                    String aId = "";
                    Account acct = null;
                    String dId = "";
                    Device dev = null;
                    String aDesc = "";
                    String dDesc = "";
                    String dName = "";
                    int dmax = 0;
                    String checked = "";
                    OrderedSet<String[]> devList;

                    // frame header
                  //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                    String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                    String editURL    = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String selectURL  = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String newURL     = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String frameTitle = _allowEdit? 
                        i18n.getString("GroupInfo.viewEditGroup","View/Edit {0} Information",grpTitles) : 
                        i18n.getString("GroupInfo.viewGroup","View {0} Information",grpTitles);
                    out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                    out.write("<hr>\n");
    
                    // group view/edit form
    
                    /* start of form */
            		JavaScriptTools.writeStartJavaScript(out);
                    JavaScriptTools.writeJSVar(out, "PARM_GROUP_MEMBERS", PARM_GROUP_MEMBERS);
                    JavaScriptTools.writeJSVar(out, "FORM_GROUP_EDIT", FORM_GROUP_EDIT); 
                	JavaScriptTools.writeEndJavaScript(out);
                    out.write("<form name='"+FORM_GROUP_EDIT+"' id='"+FORM_GROUP_EDIT+"' method='post' action='"+editURL+"' target='_self' onsubmit='onGroupEditFormSubmit()'>\n"); // target='_top'
                    out.write("<input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_UPD_GROUP+"'/>\n");
                    out.write("<input type='hidden' name='"+PARM_GROUP_MEMBERS+"' id='"+PARM_GROUP_MEMBERS+"' value=''/>\n");

                    /* Group fields */
                    out.println("<table class='"+CommonServlet.CSS_ADMIN_VIEW_TABLE+"' cellspacing='0' callpadding='0' border='0'>");
                    out.println(FormRow_TextField(PARM_GROUP_SELECT, false     , i18n.getString("GroupInfo.groupID","{0} ID",grpTitles)+":" , _selGroupID, 20, 32));
                    out.println(FormRow_TextField(PARM_GROUP_DESC  , _editGroup, i18n.getString("GroupInfo.groupName","{0} Name",grpTitles)+":", (_selGroup!=null)?_selGroup.getDescription() :"", 60, 64));
                    out.println("</table>");

                    /* Devices in group */
                    out.write("<span style='margin-left: 4px; margin-top: 8px; font-weight: bold;'>");
                    out.write(i18n.getString("GroupInfo.currentUDeviceList","Current {0} Member List",devTitles) + ":");
                    out.write("</span>\n");

                    out.write("<table cellspacing='0' cellpadding='0' border='0'><tbody><tr>\n");
                    out.write("<td>\n");
                    out.write("<div class='"+CSS_DEVICES_VIEW+"'>\n");
                    try {
                    	if(enableUniversalGroups) devList = DeviceGroup.getAllDevicesForGroup(currAcct.getAccountID(), _selGroupID, null/*User*/, true/*inclInactv*/);
                    	else devList = DeviceGroup.getArrDeviceIDsForGroup(currAcct.getAccountID(), _selGroupID, null/*User*/, true/*inclInactv*/);
                        out.write("<table cellspacing='0' cellpadding='0' id='"+PARM_TABLE_GRP_DEV+"' border='1'>\n");

                        out.write(" <thead>\n");
                        out.write("  <tr class='" +CommonServlet.CSS_ADMIN_TABLE_HEADER_ROW+"'>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL_SEL+"' nowrap>"+FilterText(i18n.getString("DeviceInfo.select","Select"))+"</th>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("GroupInfo.accountID","Account ID"))+"</th>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("SysAdminAccounts.accountName","Account Description"))+"</th>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.deviceID","{0} ID",devTitles))+"</th>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.decription","Description",devTitles))+"</th>\n");
                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.displayName","Short Name"))+"</th>\n");
                        out.write("  </tr>\n");
                        out.write(" </thead>\n");
                        out.write(" <tbody>\n");
                        if(devList!=null) {
	                        dmax = devList.size();	                        
	                		JavaScriptTools.writeStartJavaScript(out);
	                		JavaScriptTools.writeJS(out,"var GroupMembers = new Array();");
	                    	for(int d=0; d<dmax; d++) {
	                            aId = devList.get(d)[0];
	                            dId = devList.get(d)[1];
	                        	try {
	                                acct = Account.getAccount(aId);
	                                dev  = Device.getDevice(acct, dId);
	                        	} catch (DBException e) {
	        						continue;
	        					}
	                        	if( (acct==null) || (dev==null) ) continue;
	                            aDesc = acct.getDescription();
	                            dDesc = dev.getDescription();
	                            dName = dev.getDisplayName();
	                    		JavaScriptTools.writeJS(out,"var Device = new Object();");
	                    		JavaScriptTools.writeJS(out,"Device.accountID=\""+aId+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.accountDesc=\""+aDesc+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceID=\""+dId+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceDesc=\""+dDesc+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceName=\""+dName+"\";");
	                    		JavaScriptTools.writeJS(out,"GroupMembers.push(Device);");
	                    	}
	                    	JavaScriptTools.writeEndJavaScript(out);
	                        for(int d=0; d<dmax; d++) {
	                            String rowClass = ((d & 1) == 0)? 
	                                    CSS_DEVICES_DATA_ROW_ODD : 
	                                    CSS_DEVICES_DATA_ROW_EVN;
	                            aId = devList.get(d)[0];
	                            dId = devList.get(d)[1];
	                        	try {
	                                acct = Account.getAccount(aId);
	                                dev  = Device.getDevice(acct, dId);
	                        	} catch (DBException e) {
	        						continue;
	        					}
	                        	if( (acct==null) || (dev==null) ) continue;
	                            aDesc = acct.getDescription();
	                            dDesc = dev.getDescription();
	                            dName = dev.getDisplayName();
	//                            String checked    = _selDevID.equals(dev.getDeviceID())? "checked" : "";
	                            out.write("  <tr class='"+rowClass+"'>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+d+"'>");
	                            out.write("      <input type='radio' name='"+PARM_DEVICE+"' id='"+aId+ACC_DEV_SEP+dId+"' value='"+aId+ACC_DEV_SEP+dId+"' "+checked+" onclick='javascript:onGroupDeviceRadioClick(this);'></td>\n");
	                            out.write("   <td class='"+CSS_DEVICES_DATA_COL    +"' nowrap>"+aId+"</td>\n");
	                            out.write("   <td class='"+CSS_DEVICES_DATA_COL    +"' nowrap>"+aDesc+"</td>\n");
	                            out.write("   <td class='"+CSS_DEVICES_DATA_COL    +"' nowrap>"+dId+"</td>\n");
	                            out.write("   <td class='"+CSS_DEVICES_DATA_COL    +"' nowrap>"+dDesc+"</td>\n");
	                            out.write("   <td class='"+CSS_DEVICES_DATA_COL    +"' nowrap>"+dName+"</td>\n");
	                            out.write("  </tr>\n");
	                        }
                        }
                        out.write(" </tbody>\n");
                        out.write("</table>\n");
                    } catch (DBException dbe) {
                        // TODO
                    }
                    out.write("</div>\n");
                    if (_uiEdit) {               
	                    out.write("<input type=\"button\" name=\""+PARM_BUTTON_REMOVE_DEV+"\" value=\""+i18n.getString("GroupInfo.remove","Remove")+"\" onclick=\"javascript:onRemoveButtonClick('"+PARM_TABLE_ACC_DEV+"','"+PARM_TABLE_GRP_DEV+"');\">"); 
	                    out.write("<input type=\"button\" name=\""+PARM_BUTTON_ADD_DEV+"\" value=\""+i18n.getString("GroupInfo.add","Add")+"\" onclick=\"javascript:onAddButtonClick('"+PARM_TABLE_ACC_DEV+"','"+PARM_TABLE_GRP_DEV+"');\">");
                    }
                    out.write("</td>\n");
                    String selAcctId = currAcct.getAccountID();
                    String selAcctDesc = currAcct.getAccountDescription();
                    if (_uiEdit) {               
	                    try {
	                    	if(enableUniversalGroups) devList = Device.getAllDevices(true);
	                    	else devList=Device.getAllDevicesForAccount(selAcctId, true);
						} catch (DBException e) {
							devList = null;
						}
	                    if(devList!=null) {
	                    	dmax=devList.size();
	                		JavaScriptTools.writeStartJavaScript(out);
	                		JavaScriptTools.writeJS(out,"var AccountDevices = new Array();");
	                    	for(int d=0; d<dmax; d++) {
	                            aId = devList.get(d)[0];
	                            dId = devList.get(d)[1];
	                        	try {
	                                acct = Account.getAccount(aId);
	                                dev  = Device.getDevice(acct, dId);
	                        	} catch (DBException e) {
	        						continue;
	        					}
	                        	if( (acct==null) || (dev==null) ) continue;
	                            aDesc = acct.getDescription();
	                            dDesc = dev.getDescription();
	                            dName = dev.getDisplayName();
	                    		JavaScriptTools.writeJS(out,"var Device = new Object();");
	                    		JavaScriptTools.writeJS(out,"Device.accountID=\""+aId+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.accountDesc=\""+aDesc+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceID=\""+dId+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceDesc=\""+dDesc+"\";");
	                    		JavaScriptTools.writeJS(out,"Device.deviceName=\""+dName+"\";");
	                    		JavaScriptTools.writeJS(out,"AccountDevices.push(Device);");
	                    	}
	                    	JavaScriptTools.writeEndJavaScript(out);
	                    }
	                    String chooserStyle   = "height:17px; padding:0px 0px 0px 3px; margin:0px 0px 0px 3px; cursor:pointer; border:1px solid gray;";
	                    String chooserOnclick = "javascript:groupInfoShowSelector()";
	                    out.write("<td width='100%'>\n");
	                    out.write("<table cellspacing='0' cellpadding='0' border='0'>\n<tr>");
	                    out.write("<td nowrap>");
	                    out.write("<input id='"+ID_ACCOUNT_ID   +"' name='"+ID_ACCOUNT_ID     +"' type='hidden' value=''>");
	                    out.write("<input id='"+ID_ACCOUNT_DESCR+"' name='"+ID_ACCOUNT_DESCR+"' type='text' value='"+i18n.getString("GroupInfo.allAccounts","All Accounts")+"' readonly size='20' style='"+chooserStyle+"' onclick=\""+chooserOnclick+"\">");
	                    out.write("</td>\n");
//					    	TODO:                           create accountChooserDD style class                    
	                    out.write("<td style='vertical-align: bottom;'><span class='devChooserDD' id='"+ID_ACCOUNT_DD+"' onclick='"+chooserOnclick+"'>&nabla;</span></td>\n");
	                    out.write("</tr>");
	                    out.write("</table>\n");
	                    out.write("<div class='"+CSS_DEVICES_VIEW+"'>\n");
	                    if(devList!=null) {
	                        out.write("<table class='"+CommonServlet.CSS_ADMIN_SELECT_TABLE+"' id='"+PARM_TABLE_ACC_DEV+"' cellspacing='0' cellpadding='0' border='0'>\n");
	                        out.write(" <thead>\n");
	                        out.write("  <tr class='" +CommonServlet.CSS_ADMIN_TABLE_HEADER_ROW+"'>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL_SEL+"' nowrap>"+FilterText(i18n.getString("DeviceInfo.select","Select"))+"</th>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("GroupInfo.accountID","Account ID"))+"</th>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("SysAdminAccounts.accountName","Account Description"))+"</th>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.deviceID","{0} ID",devTitles))+"</th>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.decription","Description",devTitles))+"</th>\n");
	                        out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("DeviceInfo.displayName","Short Name"))+"</th>\n");
	                        out.write("  </tr>\n");
	                        out.write(" </thead>\n");
	                        
	                        out.write(" <tbody>\n");
//                            dmax=devList.size();
	                        for(int d=0; d<dmax; d++) {
	                            String rowClass = ((d & 1) == 0)? 
	                                    CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_ODD : 
	                                    CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_EVEN;
	                            aId = devList.get(d)[0];
	                            dId = devList.get(d)[1];
	                        	try {
	                                acct = Account.getAccount(aId);
	                                dev  = Device.getDevice(acct, dId);
	                        	} catch (DBException e) {
	        						continue;
	        					}
	                        	if( (acct==null) || (dev==null) ) continue;
	                            aDesc = acct.getDescription();
	                            dDesc = dev.getDescription();
	                            dName = dev.getDisplayName();
//                                String checked    = _selDevID.equals(dev.getDeviceID())? "checked" : "";
	                            out.write("  <tr class='"+rowClass+"'>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+d+"'><input type='radio' name='"+PARM_DEVICE+"' id='"+aId+ACC_DEV_SEP+dId+"' value='"+aId+ACC_DEV_SEP+dId+"' "+checked+" onclick='javascript:onAccountDeviceRadioClick(this);'></td>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+aId+"</td>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+aDesc+"</td>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+dId+"</td>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+dDesc+"</td>\n");
	                            out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+dName+"</td>\n");
	                            out.write("  </tr>\n");
	                        }
	                        out.write(" </tbody>\n");
	                        out.write("</table>\n");
	                    }
	                    out.write("</div>\n");
                    }
                    else 
                    	out.write("<td>&nbsp;");
                    out.write("</td></tr></tbody></table>\n");

                    /* end of form */
                    out.write("<hr style='margin-bottom:5px;'>\n");
                    out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                    IDDescription list[] = null;
                    if(enableUniversalGroups) {
	                    IDDescription.SortBy dcSortBy = AccountChooser.getSortBy(privLabel);
	                    java.util.List<IDDescription> idList = reqState.createAccountIDDescriptionList(false/*inclInactv*/, dcSortBy);
	                    list = idList.toArray(new IDDescription[idList.size()]);
                    }
                    else {
                    	list = new IDDescription[] { new IDDescription(selAcctId, selAcctDesc) };
                    }
                    AccountChooser.writeChooserDIV(out, reqState, list, null);
                    if (_editGroup) {
                        out.write("<input type='submit' name='"+PARM_SUBMIT_CHG+"' value='"+i18n.getString("GroupInfo.change","Change")+"'>\n");
//                        out.write("<input type='button' name='"+PARM_SUBMIT_CHG+"' value='"+i18n.getString("GroupInfo.change","Change")+"' onclick=\"javascript:onChangeButtonClick();\">\n");
                        out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                        out.write("<input type='button' name='"+PARM_BUTTON_CANCEL+"' value='"+i18n.getString("GroupInfo.cancel","Cancel")+"' onclick=\"javascript:openURL('"+editURL+"','_self');\">\n"); // target='_top'
                    } else {
                        out.write("<input type='button' name='"+PARM_BUTTON_BACK+"' value='"+i18n.getString("GroupInfo.back","Back")+"' onclick=\"javascript:openURL('"+editURL+"','_self');\">\n"); // target='_top'
                    }
                    out.write("</form>\n");

                }
            };

        } else
        if (_uiProp) {

            final boolean _editProps = _allowProp && (selGroup != null);
            HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
                public void write(PrintWriter out) throws IOException {
                    String pageName = UGroupInfo.this.getPageName();
                    String grpTitles[] = reqState.getDeviceGroupTitles();

                    // frame header
                  //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                    String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                    String editURL    = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String selectURL  = UGroupInfo.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String frameTitle = i18n.getString("GroupInfo.setDeviceProperties","Set {0} Properties",grpTitles);
                    out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                    out.write("<hr>\n");

                    /* start of form */
                    out.write("<form name='"+FORM_PROPERTY_EDIT+"' method='post' action='"+editURL+"' target='_self'>\n"); // target='_top'
                    out.write("  <input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_UPD_PROPS+"'/>\n");

                    /* Device fields */
                    out.println("  <table>");
                    out.println(FormRow_TextField(PARM_GROUP_SELECT     , false     , i18n.getString("GroupInfo.groupID","{0} ID",grpTitles)+":"         , _selGroupID, 30, 30));
                    out.println(FormRow_TextField(PARM_GROUP_DESC       , false     , i18n.getString("GroupInfo.description","{0} Name",grpTitles) +":"  , (_selGroup!=null)?_selGroup.getDescription():"", 40, 40));
                    out.println(FormRow_TextField(PARM_PROP_START_TYPE  , _editProps, i18n.getString("GroupInfo.startType","Start Type")+":"             , "", 2, 2));
                    out.println(FormRow_TextField(PARM_PROP_START_DEF   , _editProps, i18n.getString("GroupInfo.startDefinition","Start Definition")+":" , "", 5, 5));
                    out.println(FormRow_TextField(PARM_PROP_MOT_INTERV  , _editProps, i18n.getString("GroupInfo.motionInterval","In-Motion Interval")+":", "", 5, 5));
                    out.println(FormRow_TextField(PARM_PROP_STOP_TYPE   , _editProps, i18n.getString("GroupInfo.stopType","Stop Type")+":"               , "", 2, 2));
                    out.println(FormRow_TextField(PARM_PROP_STOP_INTERV , _editProps, i18n.getString("GroupInfo.stopInterval","Stop Interval")+":"       , "", 5, 5));
                    out.println(FormRow_TextField(PARM_PROP_DORM_INTERV , _editProps, i18n.getString("GroupInfo.dormantInterval","Dormant Interval")+":" , "", 5, 5));
                    out.println(FormRow_TextField(PARM_PROP_DORM_COUNT  , _editProps, i18n.getString("GroupInfo.dormantCount","Dormant Count")+":"       , "", 5, 5));
                    out.println(FormRow_TextField(PARM_PROP_EXCESS_SPEED, _editProps, i18n.getString("GroupInfo.excessSpeed","Excess Speed")+":"         , "", 5, 5));
                    out.println("  </table>");

                    /* end of form */
                    out.write("<hr>\n");
                    if (_editProps) {
                    out.write("<input type='submit' name='"+PARM_SUBMIT_QUE+"' value='"+i18n.getString("GroupInfo.queue","Queue")+"'>\n");
                    }
                    out.write("<a href='"+editURL+"' style='margin-left:20px;'>"+(_editProps?i18n.getString("GroupInfo.cancel","Cancel"):i18n.getString("GroupInfo.back","Back"))+"</a>\n");
                    out.write("</form>\n");
                    
                }
            };

        }

        /* write frame */
        String onload = error? JS_alert(true,m) : null;
        CommonServlet.writePageFrame(
            reqState,
            onload,null,                // onLoad/onUnload
            HTML_CSS,                   // Style sheets
            HTML_JS,                    // Javascript
            null,                       // Navigation
            HTML_CONTENT);              // Content

    }
    
    // ------------------------------------------------------------------------

}
