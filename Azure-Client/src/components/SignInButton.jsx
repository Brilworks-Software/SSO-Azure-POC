import Button from '@mui/material/Button';
import { useMsal, useMsalAuthentication } from '@azure/msal-react';
import axios from 'axios';
import { InteractionType } from '@azure/msal-browser';

export const SignInButton = () => {
    const { instance } = useMsal();
    const { result, error, login } = useMsalAuthentication(InteractionType.Popup, {
        scopes: ["user.read"]
    });

    const handleSignIn = () => {
        instance.loginRedirect({
            scopes: ['user.read'],
        });
        

        const postApi =async ()=>{
         const response = await  axios.post('http://localhost:8088/api/user',{
            "accessToken":result.accessToken
         })
         return response
        }
        postApi()

    }

    return (
        <Button color="inherit" onClick={handleSignIn}>Sign in</Button>
    )
};