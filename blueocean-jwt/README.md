# BlueOcean JWT Plugin

This plugin provides JWT authenticated related APIs. JWT token is signed using RSA256 algorithm. This is asymmetric 
algorithm, this means the token is signed using the private key and Client must use corresponding public key to verify 
the claims. 

# APIs

## JWT Token API

JWT token is generated for the user in session. In Jenkins there is always a user in context, that is if there is no 
logged in user then the generated token will carry the claim for anonymous user.

Default expiry time of token is 30 minutes.

JWT token is return as X-BLUEOCEAN-JWT HTTP header. 


    GET /jwt-auth/token
    
    HTTP/1.1 200 OK
    X-BLUEOCEAN-JWT: eyJraWQiOiI2M2ZhMTY0ZWRhMDk0NjNjOGZlZTI2Njg4ZjgxOTZmZCIsImFsZyI6IlJTMjU2IiwidHlwIjoiSldUIn0.eyJqdGkiOiJiMGVmMjJiNDliNWM0N2JjODU4YTg2MDdkM2Y0NGQzMyIsImlzcyI6ImJsdWVvY2Vhbi1qd3Q6Iiwic3ViIjoiYWxpY2UiLCJuYW1lIjoiQWxpY2UgQ29vcGVyIiwiaWF0IjoxNDcwMzMxNjA1LCJleHAiOjE0NzAzMzM0MDUsIm5iZiI6MTQ3MDMzMTU3NSwiY29udGV4dCI6eyJ1c2VyIjp7ImlkIjoiYWxpY2UiLCJmdWxsTmFtZSI6IkFsaWNlIENvb3BlciIsImVtYWlsIjoiYWxpY2VAamVua2lucy1jaS5vcmcifX19.H1iZAR2ajMeWRhh1VDdbqOtD7Wo0e0FZx8JDDNzphLu2DaLlxVRzBbhZ5TllvPx787kbNeK2tymFu_2Y_59qkq7YxZkrJctZTeiHVlTlHIxf2woBBggkIgoSvzNSsCcX73vjH5A5e54T5e8rUjF56XP05d5-WDvvheLo_Sqn4j19_lXkogCC2-JhDfc7sb8Xnw5PwYNZs29JYSSLOuUWm8UnD3AnBeFBhPfY2bR8-BjPXxdRWAyrZ-bz1CITfOm1xHZ-8NCGsfsUUGlcB_ijPVBt5T_29JWWFnougM1qZ_CEO56xu1572LMUmBYi8ynl75frzoSL_PvZYMXF47zcdg

JSON presentation of this token:

Header:

    {"kid":"63fa164eda09463c8fee26688f8196fd","alg":"RS256","typ":"JWT"}
    
Claims:

    {
       "name" : "Alice Cooper",
       "iss" : "blueocean-jwt:",
       "sub" : "alice",
       "exp" : 1470333405,
       "nbf" : 1470331575,
       "context" : {
          "user" : {
             "id" : "alice",
             "fullName" : "Alice Cooper",
             "email" : "alice@jenkins-ci.org"
          }
       },
       "jti" : "b0ef22b49b5c47bc858a8607d3f44d33",
       "iat" : 1470331605
    }

### Change expiry time

JWT tokens expires after 30 minutes (Default). exp claim header gives the time at which token expires. It is unix time 
in seconds. Default 30 minutes can be changed by sending expiryTimeInMins query parameter. This parameter value must be 
less than maximum expiry time allowed (8 hours or 480 minutes).   

This parameter must be used carefully, it has security implications.

    GET /jwt-auth/token?expiryTimeInMins=15

## Change maximum allowed expiry time

Use query maxExpiryTimeInMins to change default 8 hours maximum allowed expiry time.

This parameter must be used carefully, it has security implications.

    GET /jwt-auth/token?maxExpiryTimeInMins=15

## Json web key (jwk) API 

Client can call this API to get public key using the key id received as part of JWT header field 'kid'. This public key 
must be used to verify the JWT token.

    GET /jwt-auth/jwks/bab71d7b184548a6b93480721d352ba1
     
    HTTP/1.1 200 OK
    Content-type: application/json
    {
       "alg" : "RS256",
       "e" : "AQAB",
       "kty" : "RSA",
       "n" : "AMmWNNrmWzJXik7K7gmDkPumxqPzxc/JnxWsZ3CrhJGSO8hIgfsN6M5UHWSwkAoBHyNIaaPXhubWpcWCRewiI0U2Aw4jO3vzxNndRB9YaDPrrWDjvKBaqMC08IePPxmxXCj3ZS0QoEpf6rczdm2f9Of6Fro0TufXf2EYjLndBH7ep6iDQ4/TG7FkD7o39/GXuHAin0sz7atrPun3tlkuxllu5XNV+yW6WusrNIz3txyvKKEyQX950eW/6mMD0hS6yT7TbAwfrxkTnq4SiagCTllV+ct4wfnONDrao3WYgZnNgohsX/nEnYMHYq592n2WZW/i2+PNaFZlL2+3QgWO4qc=",
       "use" : "sig",
       "key_ops" : [
          "verify"
       ],
       "kid" : "bab71d7b184548a6b93480721d352ba1"
    }

An endpoint returning all the keys is also available, following the RFC 7517 format spec:

    GET /jwt-auth/jwk-set
     
    HTTP/1.1 200 OK
    Content-type: application/json
    {
        "keys": {
           "alg" : "RS256",
           "e" : "AQAB",
           "kty" : "RSA",
           "n" : "AMmWNNrmWzJXik7K7gmDkPumxqPzxc/JnxWsZ3CrhJGSO8hIgfsN6M5UHWSwkAoBHyNIaaPXhubWpcWCRewiI0U2Aw4jO3vzxNndRB9YaDPrrWDjvKBaqMC08IePPxmxXCj3ZS0QoEpf6rczdm2f9Of6Fro0TufXf2EYjLndBH7ep6iDQ4/TG7FkD7o39/GXuHAin0sz7atrPun3tlkuxllu5XNV+yW6WusrNIz3txyvKKEyQX950eW/6mMD0hS6yT7TbAwfrxkTnq4SiagCTllV+ct4wfnONDrao3WYgZnNgohsX/nEnYMHYq592n2WZW/i2+PNaFZlL2+3QgWO4qc=",
           "use" : "sig",
           "key_ops" : [
              "verify"
           ],
           "kid" : "bab71d7b184548a6b93480721d352ba1"
        }]
    }
