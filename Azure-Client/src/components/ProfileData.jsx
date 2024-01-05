import React from "react";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import ListItemAvatar from "@mui/material/ListItemAvatar";
import Avatar from "@mui/material/Avatar";
import PersonIcon from '@mui/icons-material/Person';

import MailIcon from '@mui/icons-material/Mail';

import LocationOnIcon from '@mui/icons-material/LocationOn';

export const ProfileData = ({ graphData }) => {
    return (
        <List className="profileData">
            <NameListItem name={graphData.userName} />
        </List>
    );
};

const NameListItem = ({ name }) => (
    <ListItem>
        <ListItemAvatar>
            <Avatar>
                <PersonIcon />
            </Avatar>
        </ListItemAvatar>
        <ListItemText primary="Name" secondary={name} />
    </ListItem>
);


const MailListItem = ({ mail }) => (
    <ListItem>
        <ListItemAvatar>
            <Avatar>
                <MailIcon />
            </Avatar>
        </ListItemAvatar>
        <ListItemText primary="Mail" secondary={mail} />
    </ListItem>
);


const LocationListItem = ({ id }) => (
    <ListItem>
        
        <ListItemText primary="Location" secondary={id} />
    </ListItem>
);