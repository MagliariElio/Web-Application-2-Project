<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    
    <style>
        /* Inizio del tuo CSS */
        body {
            font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
            line-height: 1.5;
            font-weight: 400;

            color-scheme: light dark;
            font-synthesis: none;
            text-rendering: optimizeLegibility;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
            background-color: #162250b2;
            display: flex;
            flex-direction: column;
            justify-content: start;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .login-container {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px #162250b2;
            width: 300px;
        }

        h1 {
            text-align: center;
            color: #162250;;
        }

        .appName {
            color: #f8f9fa;
            margin-top: 20vh;
            margin-bottom: 120px;
        }

        .form-group {
            margin-bottom: 15px;
        }

        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
        }

        input[type="text"],
input[type="password"] {
    width: 100%;
    padding: 10px;
    color: #000; 
    box-sizing: border-box;
    border: 1px solid #ccc;
    border-radius: 8px;
    background-color: #f9f9f9;
    font-size: 16px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

input[type="text"]:focus,
input[type="password"]:focus {
    border-color: #162250;
    box-shadow: 0 4px 10px #162250b2;
    background-color: #f9f9f9;
    outline: none;
}


        .primaryButton {
  background-color: #162250 !important;
  color: white !important;
  border: none !important;
  border-radius: 8px !important;
  cursor: pointer !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease,
    background-color 0.3s ease !important;
  font-size: 18px !important;
  width: 100%;
  height: 40px;
  margin-top: 20px;
}

.primaryButton:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 10px 20px #1622507a;
  background-color: #162250c2 !important;
}
    </style>
</head>
<body>
    <h1 class="appName">JOB CONNECT</h1>
    <div class="login-container">
        <h1>Login</h1>
        <form action="${url.loginAction}" method="post" id="kc-login-form" class="form">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" required autofocus class="form-control"/>
            </div>
            
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required class="form-control"/>
            </div>
            
            <div class="form-group">
                <button type="submit" class="btn btn-primary primaryButton">Login</button>
            </div>
        </form>
    </div>
</body>
</html>
