import {useEffect, useState} from 'react'
import './App.css'
import {MeInterface} from "./interfaces/MeInterface.ts";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";

import JPAPIAuth from "./apis/JPAuth.ts";
import JPNavBar from "./controllers/JSNavBar.tsx";
import JPHomePage from "./views/JPHomePage.tsx";

function App() {

    const [me, setMe] = useState<MeInterface | null>(null)
    const [role, setRole ] = useState<string>("")

    useEffect(() =>{
        JPAPIAuth.fetchMe(setMe).then(async () => {
                if(me?.principal !== null){
                    const res = await fetch("/documentStoreService/v1/API/documents/auth")
                    const json = await res.json()

                    if(JSON.stringify(json.principal.claims.realm_access.roles[0])){
                        var i = 0
                        while(json.principal.claims.realm_access.roles[i] !== "GUEST" &&
                        json.principal.claims.realm_access.roles[i] !== "OPERATOR" &&
                        json.principal.claims.realm_access.roles[i] !== "MANAGER"){
                            console.log(JSON.stringify(json.principal.claims.realm_access.roles[i]))
                            i = i + 1
                        }
                        if(json.principal.claims.realm_access.roles[i]){
                            console.log("New role: " + json.principal.claims.realm_access.roles[i])
                            setRole(json.principal.claims.realm_access.roles[i])
                        }else{
                            setRole("OPERATOR")
                        }
                    }else{
                        setRole("OPERATOR")
                    }
                }
        }
        )
    }, [])

  return (
      <Router>
          <JPNavBar me={me} />
          <Routes>
              <Route path="/ui" element={ <JPHomePage me={me} role={role} /> } />
          </Routes>
      </Router>

  )
}

export default App
