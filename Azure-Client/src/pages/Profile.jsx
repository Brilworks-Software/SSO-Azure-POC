import { ProfileData } from "../components/ProfileData";
import { useMsalAuthentication } from "@azure/msal-react";
import { InteractionType, BrowserAuthError } from "@azure/msal-browser";
import { useState, useEffect } from "react";
import { fetchData } from "../fetch";
import axios from 'axios';

export const Profile = () => {
    const { result, error, login } = useMsalAuthentication(InteractionType.Popup, {
        scopes: ["user.read"]
    });

    const [graphData, setGraphData] = useState(null);
 const handleSend=async(accessToken)=>{
    const response = await axios.post('http://localhost:8080/api/user', {
        accessToken: accessToken
    });
    if(response.data) setGraphData(response.data)
    
 }
    useEffect(() => {
        if (!!graphData) {
            return
        }

        if (!!error) {
            if (error instanceof BrowserAuthError) {
                login(InteractionType.Redirect, {
                    scopes: ["user.read"]
                })
            }
            console.log(error);
        }

        if (result) {
            handleSend(result.accessToken)
        }

    }, [error, result, graphData, login]);


    if (error) {
        return <div>Error: {error.message}</div>;
    }

    return (
        <>
            {graphData ? <ProfileData graphData={graphData} /> : null}
        </>
    )
}