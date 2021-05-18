# Sole Trader Identification Frontend Test End-Points

#### GET test-only/feature-switches

---
Shows all feature switches:
1. Sole Trader Identification Frontend

    - Use stub for Authenticator API
    - Use stub for Business Verification flow
   
#### GET test-only/create-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey.

1. Continue URL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

    - Used for the `Get help with this page` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Enable SAUTR Check (Optional)

    - Shows the user an extra page where they can enter an SAUTR
    - This is currently defaulted to false unless otherwise specified
    - If this is enabled, refer to Using the Authenticator Stub section below

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
"sautr": "1234567890",
"businessVerification": {
   "verificationStatus":"PASS"
   }
}
```

#### POST test-only/verification-question/journey

---
Stubs creating a Business Verification journey. The Business Verification Stub Feature Switch will need to be enabled.

##### Request:
No body is required for this request

##### Response:
Status: **Created(201)**

Example Response body:

```
{“redirectUri” : "/testUrl?journeyId=<businessVerificationJourneyId>"}
```

#### GET  test-only/verification-question/journey/:journeyId/status

---
Stubs retrieving the result from the Business Verification Service. The Business Verification Stub feature switch will need to be enabled.

##### Request:
A valid Business Verification journeyId must be sent in the URI

##### Response:
Status: **OK(200)**

Example Response body:
```
{
  "journeyType": "BUSINESS_VERIFICATION",
  "origin": vat,
  "identifier": {
    "saUtr" -> "1234567890"
  },
  "verificationStatus" -> "PASS"
}
```


#### Using the Authenticator stub

This stub returns different responses based on the entered last name.

`fail` & `deceased` will return a data mismatch which upon submitting CYA will redirect the user to an error page.

`no-sautr` will return the data the user has entered and allow them to pass successfully provided the enableSautrCheck boolean is false or the user has clicked the `I do not have an SAUTR` link when the boolean is enabled.

Any other last name will return the data user has entered along with `1234567890` for the SAUTR. This is the SAUTR the user must provide in order to pass validation using this stub.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
