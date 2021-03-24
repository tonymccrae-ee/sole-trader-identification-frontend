
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
Request body must contain the continueUrl.

```
{"continueUrl" : "/testUrl"}
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
{"fullName":
   {"firstName":"John",
   "lastName":"Smith"
   },
"dateOfBirth": 1978-01-05,
"nino": "AA111111A",
"sautr": "1234567890"
}
```

### Test End-Points
#### GET test-only/feature-switches

---
Shows all feature switches:
1. Sole Trader Identification Frontend

    - Use stub for Authenticator API
   
#### GET/POST test-only/create-journey

---
This is a test entry point which simulates a service making the initial call to setup a journey.

1. ContinueURL(Required)

   - Where to redirect the user after the journey has been completed

#### GET test-only/retrieve-journey/:journeyId or test-only/retrieve-journey

---
Retrieves all the journey data that is stored against a specific journeyID.

##### Request:
A valid journeyId must be sent in the URI or as a query parameter

##### Response:
Status:

| Expected Response                       | Reason
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response body:

```
{"fullName":
   {"firstName":"John",
   "lastName":"Smith"
   },
"dateOfBirth": 1978-01-05,
"nino": "AA111111A",
"sautr": "1234567890"
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
