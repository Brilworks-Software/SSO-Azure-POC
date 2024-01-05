import React from 'react';
import ReactDOM from 'react-dom/client';

import { ThemeProvider } from '@mui/material/styles';
import { theme } from "./styles/theme";

import { BrowserRouter } from "react-router-dom";

import App from './App';

import { PublicClientApplication, EventType } from '@azure/msal-browser';

const pca = new PublicClientApplication({
    auth: {
        clientId: '2b6f2082-ddac-41bd-8f35-ff4fc344cfe9',
        authority: 'https://login.microsoftonline.com/5cb1b144-b6aa-4bc9-943e-35012380be8a',
        redirectUri: '/',
        postLogoutRedirectUri: '/',
    
    },
    cache: {
        cacheLocation: 'localStorage',
        storeAuthStateInCookie: false,
    },
    system: {
        loggerOptions: {
            loggerCallback: (level, message, containsPII) => {
                console.log(message)
            },
            logLevel: "Verbose"
        }
    }
});

pca.addEventCallback((event) => {
    if (event.eventType === EventType.LOGIN_SUCCESS) {
        pca.setActiveAccount(event.payload.account);
    }
});

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
    <React.StrictMode>
        <BrowserRouter>
            <ThemeProvider theme={theme}>
                <App msalInstance={pca}/>
            </ThemeProvider>
        </BrowserRouter>
    </React.StrictMode>
);
