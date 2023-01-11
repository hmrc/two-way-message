# two-way-message

## Rendered message API

| Path                                   | Supported Methods | Description                                                    |
|----------------------------------------|-------------------|----------------------------------------------------------------|
| `/messages/:messageId/content`         | GET               | return a rendered (HTML) version of the message for a customer |

Return Status Codes:
- **200** (OK) - body contain HTML of the rendered conversation

## Note on Integration Tests

For integration tests to run successfully you need the following apps run by sm externally:
 - AUTH
 - AUTH_LOGIN_API
 - DATASTREAM
 - IDENTITY_VERIFICATION
 - MESSAGE
 - USER_DETAILS

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
