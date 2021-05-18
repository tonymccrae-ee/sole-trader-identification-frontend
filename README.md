
# Sole Trader Identification Frontend

This is a Scala/Play frontend to allow Sole Traders to provide their information to HMRC.

### How to run the service
1. Make sure any dependent services are running using the following service-manager command `sm --start SOLE_TRADER_IDENTIFICATION_ALL -r`
2. Stop the frontend in service manager using `sm --stop SOLE_TRADER_IDENTIFICATION_FRONTEND`
3. Run the frontend locally using
`sbt 'run 9717 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

### End-Points
#### POST /journey

---
Creates a new journey, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.

The enableSautrCheck field allows the calling service to decide whether to ask the user to provide 
an SAUTR that will be verified. By default "enableSautrCheck" will be false.

All other fields must be provided. 

```
{
"continueUrl" : "/test",
"optServiceName" : "Service Name",
"deskProServiceId" : "abc",
"signOutUrl" : "/sign-out",
"enableSautrCheck" : "true"
}
```

#### Response:
Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "/testUrl"}
```

#### GET /journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.
#### Request:
A valid journeyId must be sent in the URI

#### Response:
Status:

| Expected Response                       | Reason
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response body:
```
{
"firstName": "John",
"lastName": "Smith",
"dateOfBirth": 1978-01-05,
"nino": "AA111111A",
"sautr": "1234567890",
"businessVerification": {
   "verificationStatus":"PASS"
   }
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
